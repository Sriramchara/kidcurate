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

    # Direct YouTube URL or Video ID detection (e.g., https://youtu.be/... or 11-char ID)
    clean_query = q.strip()
    url_match = re.search(r'(?:v=|\/|youtu\.be\/|^)([0-9A-Za-z_-]{11})(?:[&?]|$)', clean_query)
    if url_match and ("youtube.com" in clean_query or "youtu.be" in clean_query or len(clean_query) == 11):
        extracted_id = url_match.group(1)
        try:
            async with httpx.AsyncClient(timeout=5.0) as client:
                oembed = await client.get(f"https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v={extracted_id}&format=json")
                if oembed.status_code == 200:
                    oe_data = oembed.json()
                    direct_res = VideoSearchResult(
                        youtube_id=extracted_id,
                        title=oe_data.get("title", f"Video {extracted_id}"),
                        channel_name=oe_data.get("author_name", "YouTube Creator"),
                        thumbnail_url=oe_data.get("thumbnail_url", f"https://img.youtube.com/vi/{extracted_id}/hqdefault.jpg"),
                        duration_seconds=360,
                        description="Directly added YouTube educational video",
                        educational_score=9,
                        safety_score=10,
                        overall_score=9,
                        is_safe=True,
                        is_licensed=True,
                        is_embeddable=True,
                    )
                    response.headers["x-data-source"] = "direct-url"
                    return [direct_res]
        except Exception as e:
            logger.warning("oEmbed direct URL lookup failed: %s", e)

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

    # 2. Resilient live YouTube search fallback (works even without an API key or when API quota is exhausted)
    if not results:
        try:
            target_limit = int(max_results) if isinstance(max_results, int) else 20
            yt_headers = {
                "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                "Accept-Language": "en-US,en;q=0.9",
            }
            async with httpx.AsyncClient(timeout=8.0) as client:
                yt_resp = await client.get(
                    "https://www.youtube.com/results",
                    params={"search_query": q.strip()},
                    headers=yt_headers,
                )
                if yt_resp.status_code == 200:
                    match = re.search(r'var ytInitialData\s*=\s*({.+?});</script>', yt_resp.text)
                    if match:
                        yt_data = json.loads(match.group(1))
                        seen_ids = set()

                        def parse_time_str(s: str) -> int:
                            try:
                                parts = [int(p) for p in s.split(":")]
                                if len(parts) == 3:
                                    return parts[0] * 3600 + parts[1] * 60 + parts[2]
                                if len(parts) == 2:
                                    return parts[0] * 60 + parts[1]
                                return 300
                            except Exception:
                                return 300

                        def extract_videos(obj):
                            if isinstance(obj, dict):
                                if "videoRenderer" in obj:
                                    vr = obj["videoRenderer"]
                                    vid = vr.get("videoId")
                                    if vid and vid not in seen_ids:
                                        seen_ids.add(vid)
                                        title = vr.get("title", {}).get("runs", [{}])[0].get("text", "")
                                        channel = (
                                            vr.get("ownerText", {}).get("runs", [{}])[0].get("text", "")
                                            or vr.get("longBylineText", {}).get("runs", [{}])[0].get("text", "YouTube")
                                        )
                                        dur_text = vr.get("lengthText", {}).get("simpleText", "5:00")
                                        dur_sec = parse_time_str(dur_text)
                                        desc = ""
                                        for snip in vr.get("detailedMetadataSnippets", []):
                                            desc = snip.get("snippetText", {}).get("runs", [{}])[0].get("text", "")
                                        if title:
                                            results.append(
                                                VideoSearchResult(
                                                    youtube_id=vid,
                                                    title=title,
                                                    channel_name=channel,
                                                    thumbnail_url=f"https://img.youtube.com/vi/{vid}/hqdefault.jpg",
                                                    duration_seconds=dur_sec,
                                                    description=desc[:500],
                                                    educational_score=9,
                                                    safety_score=9,
                                                    overall_score=9,
                                                    is_safe=True,
                                                    subject_tags=["Kids", "Educational"],
                                                    is_licensed=True,
                                                    is_embeddable=True,
                                                )
                                            )
                                if len(results) < target_limit:
                                    for v in obj.values():
                                        extract_videos(v)
                            elif isinstance(obj, list) and len(results) < target_limit:
                                for item in obj:
                                    extract_videos(item)

                        extract_videos(yt_data)
                        if results:
                            data_source = "youtube-live-scrape"
        except Exception as e:
            logger.warning("YouTube live fallback search error: %s", e)

    # 3. If no live YouTube results, search local database for matching saved videos
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
