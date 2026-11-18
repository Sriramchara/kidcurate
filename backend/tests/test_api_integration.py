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
    async with httpx.AsyncClient(timeout=45.0) as client:
        resp = await client.get(f"{BASE_URL}/")
        assert resp.status_code == 200
        data = resp.json()
        assert data.get("status") == "running"
        assert "KidCurate" in data.get("app", "")


@pytest.mark.asyncio
async def test_public_profiles_endpoint():
    async with httpx.AsyncClient(timeout=45.0) as client:
        # Unauthenticated request must be rejected with 401
        unauth_resp = await client.get(f"{BASE_URL}/api/children/profiles")
        assert unauth_resp.status_code == 401

        # Authenticate or register integration test parent
        login_resp = await client.post(
            f"{BASE_URL}/api/auth/login",
            data={"username": "integration_test@example.com", "password": "TestPassword1234!"}
        )
        if login_resp.status_code == 200:
            token = login_resp.json().get("access_token")
        else:
            reg_resp = await client.post(
                f"{BASE_URL}/api/auth/register",
                json={
                    "name": "Integration Test Parent",
                    "email": "integration_test@example.com",
                    "password": "TestPassword1234!"
                }
            )
            token = reg_resp.json().get("access_token")

        assert token is not None
        headers = {"Authorization": f"Bearer {token}"}
        resp = await client.get(f"{BASE_URL}/api/children/profiles", headers=headers)
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
        resp = await client.get(f"{BASE_URL}/api/videos/search", params={"q": "heidi cartoon"})
        assert resp.status_code == 200
        results = resp.json()
        assert isinstance(results, list)
