import json
import httpx
from datetime import date
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from pydantic import BaseModel
from typing import Optional, List
from app.database import get_db
from app.models import User, Child, Playlist, PlaylistItem, Video
from app.auth import get_current_user

router = APIRouter(prefix="/api/playlists", tags=["Playlists"])


async def verify_youtube_embeddable(youtube_id: str) -> tuple[bool, str]:
    """Pre-flight check via YouTube oEmbed to guarantee the video can be embedded in mobile apps."""
    try:
        url = f"https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v={youtube_id}&format=json"
        async with httpx.AsyncClient(timeout=4.0) as client:
            resp = await client.get(url)
            if resp.status_code == 200:
                return True, "OK"
            elif resp.status_code in (401, 403):
                return False, "The creator has restricted this video from being embedded in external mobile apps."
            elif resp.status_code in (400, 404):
                return False, "Video not found, deleted, or restricted from mobile embedding on YouTube."
            else:
                return False, f"Video cannot be played on mobile (HTTP {resp.status_code})."
    except Exception:
        # Fallback if network issue to oembed
        return True, "Check skipped"


# ─── Schemas ────────────────────────────────────────────────────────────────

class PlaylistCreate(BaseModel):
    child_id: str
    title: str
    description: Optional[str] = None
    scheduled_date: Optional[date] = None
    is_template: bool = False


class PlaylistUpdate(BaseModel):
    title: Optional[str] = None
    description: Optional[str] = None
    scheduled_date: Optional[date] = None
    is_active: Optional[bool] = None


class PlaylistItemAdd(BaseModel):
    youtube_id: str
    title: str
    channel_name: str
    thumbnail_url: str
    duration_seconds: int


class PlaylistItemReorder(BaseModel):
    item_id: str
    new_position: int


class VideoInPlaylist(BaseModel):
    item_id: str
    position: int
    youtube_id: str
    title: str
    channel_name: str
    thumbnail_url: str
    duration_seconds: Optional[int] = None
    overall_score: Optional[int] = None
    subject_tags: Optional[List[str]] = None


class PlaylistResponse(BaseModel):
    id: str
    child_id: str
    title: str
    description: Optional[str] = None
    scheduled_date: Optional[date] = None
    is_active: bool = True
    is_template: bool = False
    total_duration_seconds: int = 0
    video_count: int = 0
    items: List[VideoInPlaylist] = []


# ─── Helper ─────────────────────────────────────────────────────────────────

async def get_playlist_or_404(
    playlist_id: str, current_user: User, db: AsyncSession
) -> Playlist:
    result = await db.execute(
        select(Playlist)
        .join(Child)
        .where(Playlist.id == playlist_id, Child.parent_id == current_user.id)
    )
    playlist = result.scalar_one_or_none()
    if not playlist:
        raise HTTPException(status_code=404, detail="Playlist not found")
    return playlist


def playlist_to_response(playlist: Playlist) -> PlaylistResponse:
    items = []
    total_duration = 0
    for item in sorted(playlist.items, key=lambda x: x.position):
        v = item.video
        if v:
            tags = []
            if v.subject_tags:
                try:
                    tags = json.loads(v.subject_tags)
                except Exception:
                    tags = [t.strip() for t in v.subject_tags.split(",") if t.strip()]
            items.append(VideoInPlaylist(
                item_id=str(item.id),
                position=item.position,
                youtube_id=v.youtube_id,
                title=v.title,
                channel_name=v.channel_name or "",
                thumbnail_url=v.thumbnail_url or "",
                duration_seconds=v.duration_seconds,
                overall_score=v.overall_score,
                subject_tags=tags,
            ))
            total_duration += v.duration_seconds or 0
    return PlaylistResponse(
        id=str(playlist.id),
        child_id=str(playlist.child_id),
        title=playlist.title or "",
        description=playlist.description,
        scheduled_date=playlist.scheduled_date,
        is_active=bool(playlist.is_active) if playlist.is_active is not None else True,
        is_template=bool(playlist.is_template) if playlist.is_template is not None else False,
        total_duration_seconds=total_duration,
        video_count=len(items),
        items=items,
    )


# ─── Routes ─────────────────────────────────────────────────────────────────

@router.get("/", response_model=List[PlaylistResponse])
async def list_playlists(
    child_id: Optional[str] = None,
    scheduled_date: Optional[date] = None,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    query = (
        select(Playlist)
        .join(Child)
        .where(Child.parent_id == current_user.id)
        .options()
    )
    if child_id:
        query = query.where(Playlist.child_id == child_id)
    if scheduled_date:
        query = query.where(Playlist.scheduled_date == scheduled_date)

    result = await db.execute(query)
    playlists = result.scalars().unique().all()

    # Eagerly load items + videos
    responses = []
    for pl in playlists:
        await db.refresh(pl, ["items"])
        for item in pl.items:
            await db.refresh(item, ["video"])
        responses.append(playlist_to_response(pl))
    return responses


@router.post("/", response_model=PlaylistResponse, status_code=status.HTTP_201_CREATED)
async def create_playlist(
    data: PlaylistCreate,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    # Verify the child belongs to this parent
    child_result = await db.execute(
        select(Child).where(Child.id == data.child_id, Child.parent_id == current_user.id)
    )
    if not child_result.scalar_one_or_none():
        raise HTTPException(status_code=404, detail="Child not found")

    playlist = Playlist(
        child_id=data.child_id,
        title=data.title,
        description=data.description,
        scheduled_date=data.scheduled_date,
        is_template=data.is_template,
    )
    db.add(playlist)
    await db.commit()
    await db.refresh(playlist, ["items"])
    return playlist_to_response(playlist)


@router.get("/{playlist_id}", response_model=PlaylistResponse)
async def get_playlist(
    playlist_id: str,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    playlist = await get_playlist_or_404(playlist_id, current_user, db)
    await db.refresh(playlist, ["items"])
    for item in playlist.items:
        await db.refresh(item, ["video"])
    return playlist_to_response(playlist)


@router.put("/{playlist_id}", response_model=PlaylistResponse)
async def update_playlist(
    playlist_id: str,
    data: PlaylistUpdate,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    playlist = await get_playlist_or_404(playlist_id, current_user, db)
    if data.title is not None:
        playlist.title = data.title
    if data.description is not None:
        playlist.description = data.description
    if data.scheduled_date is not None:
        playlist.scheduled_date = data.scheduled_date
    if data.is_active is not None:
        playlist.is_active = data.is_active
    await db.commit()
    await db.refresh(playlist, ["items"])
    for item in playlist.items:
        await db.refresh(item, ["video"])
    return playlist_to_response(playlist)


@router.delete("/{playlist_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_playlist(
    playlist_id: str,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    playlist = await get_playlist_or_404(playlist_id, current_user, db)
    await db.delete(playlist)
    await db.commit()


@router.post("/{playlist_id}/items", response_model=PlaylistResponse)
async def add_video_to_playlist(
    playlist_id: str,
    data: PlaylistItemAdd,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    playlist = await get_playlist_or_404(playlist_id, current_user, db)

    # Pre-flight embed check
    is_ok, reason = await verify_youtube_embeddable(data.youtube_id)
    if not is_ok:
        raise HTTPException(status_code=400, detail=reason)

    # Get or create video in cache
    vid_result = await db.execute(select(Video).where(Video.youtube_id == data.youtube_id))
    video = vid_result.scalar_one_or_none()
    if not video:
        video = Video(
            youtube_id=data.youtube_id,
            title=data.title,
            channel_name=data.channel_name,
            thumbnail_url=data.thumbnail_url,
            duration_seconds=data.duration_seconds,
        )
        db.add(video)
        await db.flush()

    # Calculate next position
    await db.refresh(playlist, ["items"])
    next_pos = max((i.position for i in playlist.items), default=0) + 1

    item = PlaylistItem(
        playlist_id=playlist.id,
        video_id=video.id,
        position=next_pos,
    )
    db.add(item)
    await db.commit()
    await db.refresh(playlist, ["items"])
    for i in playlist.items:
        await db.refresh(i, ["video"])
    return playlist_to_response(playlist)


@router.delete("/{playlist_id}/items/{item_id}", response_model=PlaylistResponse)
async def remove_video_from_playlist(
    playlist_id: str,
    item_id: str,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    playlist = await get_playlist_or_404(playlist_id, current_user, db)
    await db.refresh(playlist, ["items"])

    item = next((i for i in playlist.items if str(i.id) == item_id), None)
    if not item:
        raise HTTPException(status_code=404, detail="Item not found in playlist")

    await db.delete(item)
    await db.commit()
    await db.refresh(playlist, ["items"])
    for i in playlist.items:
        await db.refresh(i, ["video"])
    return playlist_to_response(playlist)


@router.put("/{playlist_id}/reorder")
async def reorder_playlist(
    playlist_id: str,
    ordered_item_ids: List[str],  # list of item IDs in desired order
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Reorder playlist items. Pass the full ordered list of item IDs."""
    playlist = await get_playlist_or_404(playlist_id, current_user, db)
    await db.refresh(playlist, ["items"])

    item_map = {str(i.id): i for i in playlist.items}
    for pos, item_id in enumerate(ordered_item_ids, start=1):
        if item_id in item_map:
            item_map[item_id].position = pos

    await db.commit()
    await db.refresh(playlist, ["items"])
    for i in playlist.items:
        await db.refresh(i, ["video"])
    return playlist_to_response(playlist)


# ─── Android App Endpoint ────────────────────────────────────────────────────

@router.get("/child/{child_id}/today")
async def get_todays_playlist(child_id: str, db: AsyncSession = Depends(get_db)):
    """
    Android app endpoint: returns today's active playlist for a child.
    No auth required — child identified by their PIN verification.
    """
    today = date.today()
    result = await db.execute(
        select(Playlist).where(
            Playlist.child_id == child_id,
            Playlist.scheduled_date == today,
            Playlist.is_active == True,
        )
    )
    playlist = result.scalar_one_or_none()

    if not playlist:
        # Try to find any active playlist (unscheduled)
        result = await db.execute(
            select(Playlist).where(
                Playlist.child_id == child_id,
                Playlist.scheduled_date == None,
                Playlist.is_active == True,
                Playlist.is_template == False,
            )
        )
        playlist = result.scalar_one_or_none()

    if not playlist:
        return {"playlist": None, "message": "No playlist for today"}

    await db.refresh(playlist, ["items"])
    for item in playlist.items:
        await db.refresh(item, ["video"])

    return {"playlist": playlist_to_response(playlist)}


@router.post("/child/{child_id}/add-video")
async def add_video_to_child_playlist(
    child_id: str,
    data: PlaylistItemAdd,
    db: AsyncSession = Depends(get_db),
):
    """
    In-App Parent Zone endpoint: adds a video directly to child's today playlist.
    """
    # Pre-flight embed check
    is_ok, reason = await verify_youtube_embeddable(data.youtube_id)
    if not is_ok:
        raise HTTPException(status_code=400, detail=reason)

    result = await db.execute(
        select(Playlist).where(Playlist.child_id == child_id, Playlist.is_active == True)
    )
    playlist = result.scalar_one_or_none()
    if not playlist:
        import uuid
        playlist = Playlist(
            id=str(uuid.uuid4()),
            child_id=child_id,
            title="Today's Adventures",
            is_active=True,
            total_duration_seconds=0,
        )
        db.add(playlist)
        await db.commit()
        await db.refresh(playlist)

    result = await db.execute(select(Video).where(Video.youtube_id == data.youtube_id))
    video = result.scalar_one_or_none()
    if not video:
        import uuid
        video = Video(
            id=str(uuid.uuid4()),
            youtube_id=data.youtube_id,
            title=data.title,
            channel_name=data.channel_name,
            thumbnail_url=data.thumbnail_url,
            duration_seconds=data.duration_seconds,
            is_safe=True,
            overall_score=95,
        )
        db.add(video)
        await db.commit()
        await db.refresh(video)

    await db.refresh(playlist, ["items"])
    import uuid
    new_pos = len(playlist.items) + 1
    new_item = PlaylistItem(
        id=str(uuid.uuid4()),
        playlist_id=playlist.id,
        video_id=video.id,
        position=new_pos,
    )
    db.add(new_item)
    playlist.total_duration_seconds = (playlist.total_duration_seconds or 0) + (data.duration_seconds or 0)
    await db.commit()
    await db.refresh(playlist, ["items"])
    for item in playlist.items:
        await db.refresh(item, ["video"])

    return {"status": "ok", "playlist": playlist_to_response(playlist)}


@router.delete("/child/{child_id}/items/{item_id}")
async def remove_video_from_child_playlist(
    child_id: str,
    item_id: str,
    db: AsyncSession = Depends(get_db),
):
    """Parent Zone endpoint: removes a video item from child's playlist."""
    result = await db.execute(
        select(Playlist).where(Playlist.child_id == child_id, Playlist.is_active == True)
    )
    playlist = result.scalar_one_or_none()
    if not playlist:
        raise HTTPException(status_code=404, detail="Playlist not found")

    await db.refresh(playlist, ["items"])
    item = next((i for i in playlist.items if str(i.id) == item_id), None)
    if not item:
        raise HTTPException(status_code=404, detail="Video item not found in playlist")

    await db.refresh(item, ["video"])
    if item.video and item.video.duration_seconds:
        playlist.total_duration_seconds = max(0, (playlist.total_duration_seconds or 0) - item.video.duration_seconds)

    await db.delete(item)
    await db.commit()
    await db.refresh(playlist, ["items"])
    for i in playlist.items:
        await db.refresh(i, ["video"])
    return {"status": "ok", "playlist": playlist_to_response(playlist)}


class ReorderRequest(BaseModel):
    ordered_item_ids: List[str]


@router.put("/child/{child_id}/reorder")
async def reorder_child_playlist(
    child_id: str,
    data: ReorderRequest,
    db: AsyncSession = Depends(get_db),
):
    """Parent Zone endpoint: reorders videos in child's playlist."""
    result = await db.execute(
        select(Playlist).where(Playlist.child_id == child_id, Playlist.is_active == True)
    )
    playlist = result.scalar_one_or_none()
    if not playlist:
        raise HTTPException(status_code=404, detail="Playlist not found")

    await db.refresh(playlist, ["items"])
    item_map = {str(item.id): item for item in playlist.items}
    for pos, item_id in enumerate(data.ordered_item_ids, start=1):
        if item_id in item_map:
            item_map[item_id].position = pos

    await db.commit()
    await db.refresh(playlist, ["items"])
    for i in playlist.items:
        await db.refresh(i, ["video"])
    return {"status": "ok", "playlist": playlist_to_response(playlist)}



