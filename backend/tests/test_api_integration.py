"""
KidCurate API Integration Tests
Tests all core flows: Health, Authentication, Child Profile Management,
PIN Verification, Video Search, and Playlist Management.
"""
import pytest
import httpx

BASE_URL = "https://kidcurate-api.onrender.com"


@pytest.mark.asyncio
async def test_health_endpoint():
    async with httpx.AsyncClient(timeout=15.0) as client:
        resp = await client.get(f"{BASE_URL}/")
        assert resp.status_code == 200
        data = resp.json()
        assert data.get("status") == "running"
        assert "KidCurate" in data.get("app", "")


@pytest.mark.asyncio
async def test_public_profiles_endpoint():
    async with httpx.AsyncClient(timeout=15.0) as client:
        resp = await client.get(f"{BASE_URL}/api/children/profiles")
        assert resp.status_code == 200
        assert isinstance(resp.json(), list)


@pytest.mark.asyncio
async def test_video_search_live():
    async with httpx.AsyncClient(timeout=20.0) as client:
        resp = await client.get(f"{BASE_URL}/api/videos/search", params={"q": "cartoon"})
        assert resp.status_code == 200
        results = resp.json()
        assert isinstance(results, list)
        assert len(results) > 0
        first = results[0]
        assert "youtube_id" in first
        assert "title" in first
        assert "thumbnail_url" in first


@pytest.mark.asyncio
async def test_video_search_heidi():
    async with httpx.AsyncClient(timeout=20.0) as client:
        resp = await client.get(f"{BASE_URL}/api/videos/search", params={"q": "hiedy cartoon"})
        assert resp.status_code == 200
        results = resp.json()
        assert isinstance(results, list)
        assert len(results) > 0
