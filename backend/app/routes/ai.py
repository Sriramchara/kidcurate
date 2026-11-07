from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from typing import List, Optional, Dict, Any
from datetime import date, timedelta
import httpx
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from app.database import get_db
from app.auth import get_current_user
from app.models import User, Playlist, PlaylistItem, Video, Child
import uuid
from app.services.ai_scorer import generate_quiz, generate_curriculum
from app.config import settings
import json
from app.routes.videos import YOUTUBE_SEARCH_URL, YOUTUBE_VIDEOS_URL, parse_iso_duration

router = APIRouter(prefix="/api/ai", tags=["AI"])





class CurriculumRequest(BaseModel):
    child_age: int = 7
    subject: str
    goals: Optional[str] = ""
    num_days: Optional[int] = 5


@router.post("/curriculum")
async def create_ai_curriculum(
    data: CurriculumRequest,
    current_user: User = Depends(get_current_user),
):
    """Generate an AI learning roadmap/curriculum tailored to age and subject."""
    curriculum = await generate_curriculum(
        child_age=data.child_age,
        subject=data.subject,
        goals=data.goals or "",
        num_days=min(max(1, data.num_days or 5), 14),
    )
    if not curriculum:
        raise HTTPException(status_code=500, detail="Curriculum generation failed")
    return curriculum


@router.get("/quiz/{youtube_id}")
async def get_video_quiz(
    youtube_id: str,
    db: AsyncSession = Depends(get_db)
):
    """Get or generate an AI quiz for a specific video."""
    # Check if video exists and has cached quiz
    db_vid = await db.execute(select(Video).where(Video.youtube_id == youtube_id))
    video = db_vid.scalar_one_or_none()
    
    if not video:
        raise HTTPException(status_code=404, detail="Video not found in database")
        
    if video.quiz_json:
        return json.loads(video.quiz_json)
        
    # Generate new quiz
    quiz = await generate_quiz(youtube_id, video.title, video.description or "")
    if not quiz:
        raise HTTPException(status_code=503, detail="Could not generate quiz from transcript")
        
    # Cache it
    video.quiz_json = json.dumps(quiz)
    await db.commit()
    
    return quiz


class ApplyCurriculumRequest(BaseModel):
    child_id: str
    curriculum: Dict[str, Any]


@router.post("/curriculum/apply")
async def apply_ai_curriculum(
    data: ApplyCurriculumRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Convert an AI-generated curriculum into real scheduled playlists with videos."""
    res = await db.execute(select(Child).where(Child.id == data.child_id, Child.parent_id == current_user.id))
    child = res.scalar_one_or_none()
    if not child:
        raise HTTPException(status_code=404, detail="Child profile not found")

    curriculum = data.curriculum
    days = curriculum.get("days", [])
    if not days:
        raise HTTPException(status_code=400, detail="Curriculum contains no daily plans")

    created_playlists = []
    today = date.today()

    for idx, d in enumerate(days):
        day_num = d.get("day_number", idx + 1)
        topic = d.get("topic", f"Lesson {day_num}")
        scheduled_date = (today + timedelta(days=idx)).isoformat()

        playlist = Playlist(
            id=str(uuid.uuid4()),
            child_id=child.id,
            title=f"Day {day_num}: {topic}",
            description=d.get("objective", ""),
            scheduled_date=scheduled_date,
            is_active=True,
            is_template=False,
            total_duration_seconds=0,
            video_count=0,
        )
        db.add(playlist)
        await db.flush()

        added_videos = []
        keywords = [w.lower() for w in topic.split() if len(w) > 3]
        for kw in keywords:
            stmt = select(Video).where(Video.is_safe == True, Video.title.ilike(f"%{kw}%")).limit(2)
            match_res = await db.execute(stmt)
            matches = match_res.scalars().all()
            for m in matches:
                if m.id not in [v.id for v in added_videos]:
                    added_videos.append(m)
                if len(added_videos) >= 2:
                    break
            if len(added_videos) >= 2:
                break

        if len(added_videos) < 2:
            fallback_res = await db.execute(select(Video).where(Video.is_safe == True).limit(5))
            all_safe = fallback_res.scalars().all()
            for s in all_safe:
                if s.id not in [v.id for v in added_videos]:
                    added_videos.append(s)
                if len(added_videos) >= 2:
                    break

        total_secs = 0
        for pos, vid in enumerate(added_videos, 1):
            p_item = PlaylistItem(
                id=str(uuid.uuid4()),
                playlist_id=playlist.id,
                video_id=vid.id,
                position=pos,
            )
            db.add(p_item)
            total_secs += (vid.duration_seconds or 300)

        playlist.video_count = len(added_videos)
        playlist.total_duration_seconds = total_secs
        created_playlists.append({
            "id": playlist.id,
            "title": playlist.title,
            "scheduled_date": playlist.scheduled_date,
            "video_count": playlist.video_count,
            "total_duration_seconds": playlist.total_duration_seconds,
        })

    await db.commit()
    return {"status": "success", "count": len(created_playlists), "playlists": created_playlists}

