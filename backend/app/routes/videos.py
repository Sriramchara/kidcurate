import httpx
import json
import logging
import re
from datetime import datetime, timezone
from fastapi import APIRouter, Depends, HTTPException, Query, Response
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, or_
from pydantic import BaseModel
from typing import Optional, List
from app.database import get_db
from app.models import User, Video
from app.auth import get_optional_current_user
from app.config import settings
from app.services.ai_scorer import score_video

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/videos", tags=["Videos"])

YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search"
YOUTUBE_VIDEOS_URL = "https://www.googleapis.com/youtube/v3/videos"


class VideoSearchResult(BaseModel):
    youtube_id: str
    title: str
    channel_name: str
    thumbnail_url: str
    duration_seconds: Optional[int] = None
    description: Optional[str] = None
    educational_score: Optional[int] = None
    safety_score: Optional[int] = None
    overall_score: Optional[int] = None
    ai_summary: Optional[str] = None
    is_safe: Optional[bool] = None
    subject_tags: Optional[List[str]] = None
    is_licensed: Optional[bool] = None
    is_embeddable: Optional[bool] = None


def parse_iso_duration(duration: str) -> int:
    """Parse ISO 8601 duration (e.g. PT4M13S) to seconds."""
    match = re.match(r'PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?', duration)
    if not match:
        return 0
    hours = int(match.group(1) or 0)
    minutes = int(match.group(2) or 0)
    seconds = int(match.group(3) or 0)
    return hours * 3600 + minutes * 60 + seconds


@router.get("/search", response_model=List[VideoSearchResult])
async def search_videos(
    response: Response,
    q: str = Query(..., description="Search query, e.g. 'Moral Stories for Kids'"),
    age: Optional[int] = Query(None, description="Optional child age hint"),
    max_results: int = Query(20, le=50),
    ai_score: bool = Query(False, description="Run AI scoring on results"),
    db: AsyncSession = Depends(get_db),
    current_user: Optional[User] = Depends(get_optional_current_user),
):
    """
    Search YouTube live for educational videos with strict child safety filters.
    If the YouTube API is unavailable, searches previously saved videos in the local database.
    """
    results: List[VideoSearchResult] = []
    data_source = "none"

    # 1. Live YouTube API Search
    api_key = settings.YOUTUBE_API_KEY
    if api_key and not api_key.startswith("your-"):
        try:
            async with httpx.AsyncClient(timeout=8.0) as client:
                search_params = {
                    "part": "snippet",
                    "q": q,
                    "type": "video",
                    "safeSearch": "strict",
                    "videoEmbeddable": "true",
                    "relevanceLanguage": "en",
                    "maxResults": max_results,
                    "key": api_key,
                }
                search_resp = await client.get(YOUTUBE_SEARCH_URL, params=search_params)

                if search_resp.status_code == 200:
                    search_data = search_resp.json()
                    video_ids = [
                        item["id"]["videoId"]
                        for item in search_data.get("items", [])
                        if "videoId" in item.get("id", {})
                    ]

                    if video_ids:
                        details_resp = await client.get(
                            YOUTUBE_VIDEOS_URL,
                            params={
                                "part": "contentDetails,statistics,snippet,status",
                                "id": ",".join(video_ids),
                                "key": api_key,
                            },
                        )
                        if details_resp.status_code == 200:
                            details_data = details_resp.json()
                            for item in details_data.get("items", []):
                                vid_id = item["id"]
                                snippet = item["snippet"]
                                content = item.get("contentDetails", {})
                                status = item.get("status", {})
                                duration_str = content.get("duration", "PT0S")
                                duration_secs = parse_iso_duration(duration_str)
                                is_licensed = content.get("licensedContent", False)
                                is_embeddable = status.get("embeddable", True)

                                # Child safe rules: embeddable and reasonable length
                                if not is_embeddable or duration_secs < 60 or duration_secs > 3600:
                                    continue

                                thumbnails = snippet.get("thumbnails", {})
                                thumb = (
                                    thumbnails.get("high")
                                    or thumbnails.get("medium")
                                    or thumbnails.get("default")
                                    or {}
                                ).get("url", f"https://img.youtube.com/vi/{vid_id}/hqdefault.jpg")

                                # Check if already scored/cached in DB
                                db_result = await db.execute(select(Video).where(Video.youtube_id == vid_id))
                                cached_video = db_result.scalar_one_or_none()

                                res_item = VideoSearchResult(
                                    youtube_id=vid_id,
                                    title=snippet.get("title", ""),
                                    channel_name=snippet.get("channelTitle", ""),
                                    thumbnail_url=thumb,
                                    duration_seconds=duration_secs,
                                    description=snippet.get("description", "")[:500],
                                    is_licensed=is_licensed,
                                    is_embeddable=is_embeddable,
                                )

                                if cached_video and cached_video.overall_score is not None:
                                    res_item.educational_score = cached_video.educational_score
                                    res_item.safety_score = cached_video.safety_score
                                    res_item.overall_score = cached_video.overall_score
                                    res_item.ai_summary = cached_video.ai_summary
                                    res_item.is_safe = cached_video.is_safe
                                    res_item.subject_tags = json.loads(cached_video.subject_tags or "[]")
                                elif ai_score:
                                    scores = await score_video(
                                        youtube_id=vid_id,
                                        title=res_item.title,
                                        description=res_item.description or "",
                                        channel_name=res_item.channel_name,
                                        duration_seconds=duration_secs,
                                        target_age=age,
                                    )
                                    if scores:
                                        res_item.educational_score = scores.get("educational_score")
                                        res_item.safety_score = scores.get("safety_score")
                                        res_item.overall_score = scores.get("overall_score")
                                        res_item.ai_summary = scores.get("summary")
                                        res_item.is_safe = scores.get("is_safe", True)
                                        res_item.subject_tags = scores.get("subject_tags", [])

                                results.append(res_item)
                            data_source = "youtube-api"
                else:
                    logger.warning("YouTube API error (%s): %s", search_resp.status_code, search_resp.text)
        except Exception as e:
            logger.warning("Live YouTube API query failed: %s", e)

    # 2. If no live YouTube results, search local database for matching saved videos
    if not results:
        try:
            clean_q = q.strip()
            cached_db = await db.execute(
                select(Video).where(
                    or_(
                        Video.title.ilike(f"%{clean_q}%"),
                        Video.description.ilike(f"%{clean_q}%"),
                    )
                ).limit(max_results)
            )
            for cv in cached_db.scalars().all():
                results.append(
                    VideoSearchResult(
                        youtube_id=cv.youtube_id,
                        title=cv.title,
                        channel_name=cv.channel_name or "Educational Channel",
                        thumbnail_url=cv.thumbnail_url or f"https://img.youtube.com/vi/{cv.youtube_id}/hqdefault.jpg",
                        duration_seconds=cv.duration_seconds or 300,
                        description=cv.description,
                        educational_score=cv.educational_score,
                        safety_score=cv.safety_score,
                        overall_score=cv.overall_score,
                        ai_summary=cv.ai_summary,
                        is_safe=cv.is_safe,
                        subject_tags=json.loads(cv.subject_tags or "[]"),
                        is_licensed=True,
                        is_embeddable=True,
                    )
                )
            if results:
                data_source = "local-db"
        except Exception as err:
            logger.warning("Local DB search error: %s", err)

    response.headers["x-data-source"] = data_source
    return results


@router.post("/cache/{youtube_id}")
async def cache_video(
    youtube_id: str,
    db: AsyncSession = Depends(get_db),
    current_user: Optional[User] = Depends(get_optional_current_user),
):
    """Cache a YouTube video in the local database by ID."""
    # Check if already cached
    result = await db.execute(select(Video).where(Video.youtube_id == youtube_id))
    existing = result.scalar_one_or_none()
    if existing:
        return {"video_id": str(existing.id), "cached": False, "message": "Already cached"}

    # Query YouTube API for live metadata
    api_key = settings.YOUTUBE_API_KEY
    if api_key and not api_key.startswith("your-"):
        try:
            async with httpx.AsyncClient(timeout=8.0) as client:
                resp = await client.get(
                    YOUTUBE_VIDEOS_URL,
                    params={
                        "part": "snippet,contentDetails",
                        "id": youtube_id,
                        "key": api_key,
                    },
                )
                if resp.status_code == 200:
                    data = resp.json()
                    items = data.get("items", [])
                    if items:
                        item = items[0]
                        snippet = item["snippet"]
                        duration_secs = parse_iso_duration(item["contentDetails"]["duration"])
                        thumbnails = snippet.get("thumbnails", {})
                        thumb = (
                            thumbnails.get("high")
                            or thumbnails.get("medium")
                            or {}
                        ).get("url", f"https://img.youtube.com/vi/{youtube_id}/hqdefault.jpg")

                        video = Video(
                            youtube_id=youtube_id,
                            title=snippet["title"],
                            channel_name=snippet["channelTitle"],
                            channel_id=snippet.get("channelId"),
                            duration_seconds=duration_secs,
                            thumbnail_url=thumb,
                            description=snippet.get("description", "")[:2000],
                        )
                        db.add(video)
                        await db.commit()
                        await db.refresh(video)
                        return {"video_id": str(video.id), "cached": True}
        except Exception as e:
            logger.warning("Failed to fetch video details from YouTube API: %s", e)

    # Standard fallback record if YouTube API network request is blocked
    video = Video(
        youtube_id=youtube_id,
        title=f"Video ({youtube_id})",
        channel_name="YouTube Video",
        duration_seconds=300,
        thumbnail_url=f"https://img.youtube.com/vi/{youtube_id}/hqdefault.jpg",
        description="",
    )
    db.add(video)
    await db.commit()
    await db.refresh(video)
    return {"video_id": str(video.id), "cached": True}
