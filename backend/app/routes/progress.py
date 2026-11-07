from datetime import datetime, timezone
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func
from pydantic import BaseModel
from typing import Optional
from app.database import get_db
from app.models import User, Child, WatchProgress, Video, PlaylistItem
from app.auth import get_current_user

router = APIRouter(prefix="/api/progress", tags=["Progress"])


class ProgressUpdate(BaseModel):
    child_id: str
    youtube_id: str
    playlist_item_id: Optional[str] = None
    watched_seconds: int
    completed: bool = False


class ProgressResponse(BaseModel):
    total_videos_watched: int
    total_watch_time_seconds: int
    completed_videos: int
    this_week_seconds: int
    favorite_subjects: list[str] = []


@router.post("/update")
async def update_progress(data: ProgressUpdate, db: AsyncSession = Depends(get_db)):
    """
    Called by Android app when video is played or completed.
    No parent auth needed — child session just needs child_id.
    """
    # Verify child exists
    child_result = await db.execute(select(Child).where(Child.id == data.child_id))
    if not child_result.scalar_one_or_none():
        raise HTTPException(status_code=404, detail="Child not found")

    # Get video by youtube_id
    vid_result = await db.execute(select(Video).where(Video.youtube_id == data.youtube_id))
    video = vid_result.scalar_one_or_none()
    if not video:
        video = Video(
            youtube_id=data.youtube_id,
            title=f"Video {data.youtube_id}",
            channel_name="YouTube",
            duration_seconds=max(data.watched_seconds, 120),
        )
        db.add(video)
        await db.commit()
        await db.refresh(video)

    # Upsert progress (find existing or create new)
    prog_result = await db.execute(
        select(WatchProgress).where(
            WatchProgress.child_id == data.child_id,
            WatchProgress.video_id == video.id,
            WatchProgress.playlist_item_id == data.playlist_item_id,
        )
    )
    progress = prog_result.scalar_one_or_none()

    if progress:
        progress.watched_seconds = max(progress.watched_seconds, data.watched_seconds)
        if data.completed and not progress.completed:
            progress.completed = True
            progress.completed_at = datetime.now(timezone.utc)
    else:
        progress = WatchProgress(
            child_id=data.child_id,
            video_id=video.id,
            playlist_item_id=data.playlist_item_id,
            watched_seconds=data.watched_seconds,
            completed=data.completed,
            completed_at=datetime.now(timezone.utc) if data.completed else None,
        )
        db.add(progress)

    await db.commit()
    return {"success": True}


@router.get("/child/{child_id}/summary", response_model=ProgressResponse)
async def get_child_progress(
    child_id: str,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Dashboard summary for parent — total stats for a child."""
    # Verify child belongs to this parent
    child_result = await db.execute(
        select(Child).where(Child.id == child_id, Child.parent_id == current_user.id)
    )
    if not child_result.scalar_one_or_none():
        raise HTTPException(status_code=404, detail="Child not found")

    # Total videos
    total_result = await db.execute(
        select(func.count(WatchProgress.id)).where(WatchProgress.child_id == child_id)
    )
    total_videos = total_result.scalar_one() or 0

    # Total watch time
    time_result = await db.execute(
        select(func.sum(WatchProgress.watched_seconds)).where(WatchProgress.child_id == child_id)
    )
    total_seconds = time_result.scalar_one() or 0

    # Completed videos
    completed_result = await db.execute(
        select(func.count(WatchProgress.id)).where(
            WatchProgress.child_id == child_id,
            WatchProgress.completed == True,
        )
    )
    completed = completed_result.scalar_one() or 0

    # This week
    from datetime import date, timedelta
    week_start = date.today() - timedelta(days=date.today().weekday())
    week_result = await db.execute(
        select(func.sum(WatchProgress.watched_seconds)).where(
            WatchProgress.child_id == child_id,
            WatchProgress.watched_at >= week_start,
        )
    )
    week_seconds = week_result.scalar_one() or 0

    # Favorite subjects
    subject_result = await db.execute(
        select(Video.subject, func.count(WatchProgress.id))
        .join(WatchProgress, WatchProgress.video_id == Video.id)
        .where(WatchProgress.child_id == child_id, Video.subject != None)
        .group_by(Video.subject)
        .order_by(func.count(WatchProgress.id).desc())
        .limit(3)
    )
    subjects = [row[0] for row in subject_result.all() if row[0]]

    return ProgressResponse(
        total_videos_watched=total_videos,
        total_watch_time_seconds=total_seconds,
        completed_videos=completed,
        this_week_seconds=week_seconds,
        favorite_subjects=subjects,
    )


@router.get("/child/{child_id}/history")
async def get_watch_history(
    child_id: str,
    limit: int = 20,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Recent watch history for a child — for parent dashboard."""
    child_result = await db.execute(
        select(Child).where(Child.id == child_id, Child.parent_id == current_user.id)
    )
    if not child_result.scalar_one_or_none():
        raise HTTPException(status_code=404, detail="Child not found")

    result = await db.execute(
        select(WatchProgress)
        .where(WatchProgress.child_id == child_id)
        .order_by(WatchProgress.watched_at.desc())
        .limit(limit)
    )
    records = result.scalars().all()

    history = []
    for rec in records:
        await db.refresh(rec, ["video"])
        if rec.video:
            history.append({
                "youtube_id": rec.video.youtube_id,
                "title": rec.video.title,
                "thumbnail_url": rec.video.thumbnail_url,
                "watched_seconds": rec.watched_seconds,
                "duration_seconds": rec.video.duration_seconds,
                "completed": rec.completed,
                "watched_at": rec.watched_at.isoformat() if rec.watched_at else None,
            })
    return {"history": history}
