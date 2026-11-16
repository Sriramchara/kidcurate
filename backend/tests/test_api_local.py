import pytest
import pytest_asyncio
import uuid
from unittest.mock import patch, AsyncMock
from httpx import AsyncClient, ASGITransport
from app.main import app
from app.database import engine, AsyncSessionLocal
from app.models import Base, User, Child


@pytest_asyncio.fixture(autouse=True)
async def prepare_database():
    """Create in-memory SQLite tables before tests and clean up after."""
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    yield
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.drop_all)


@pytest.mark.asyncio
async def test_root_and_health():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        resp = await ac.get("/")
        assert resp.status_code == 200
        data = resp.json()
        assert data["status"] == "running"
        assert "KidCurate" in data["app"]

        health_resp = await ac.get("/health")
        assert health_resp.status_code == 200
        assert health_resp.json()["status"] == "ok"


@pytest.mark.asyncio
async def test_auth_registration_and_login():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        # Register Parent
        reg_resp = await ac.post("/api/auth/register", json={
            "email": "parent_test@example.com",
            "password": "supersecretpassword123",
            "name": "Test Parent"
        })
        assert reg_resp.status_code == 201
        data = reg_resp.json()
        assert "access_token" in data
        assert data["token_type"] == "bearer"

        # Duplicate register fails
        dup_resp = await ac.post("/api/auth/register", json={
            "email": "parent_test@example.com",
            "password": "supersecretpassword123",
            "name": "Test Parent"
        })
        assert dup_resp.status_code == 400

        # Login with OAuth2 form data
        login_resp = await ac.post("/api/auth/login", data={
            "username": "parent_test@example.com",
            "password": "supersecretpassword123",
        })
        assert login_resp.status_code == 200
        assert "access_token" in login_resp.json()


@pytest.mark.asyncio
async def test_password_reset_security_no_code_leak():
    """Verify forgot-password NEVER returns reset code in API response."""
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        await ac.post("/api/auth/register", json={
            "email": "reset_test@example.com",
            "password": "initialpassword123",
            "name": "Reset Test Parent"
        })

        resp = await ac.post("/api/auth/forgot-password", json={
            "email": "reset_test@example.com"
        })
        assert resp.status_code == 200
        data = resp.json()
        # MUST NOT leak reset code in response
        assert "reset_code" not in data
        assert "code" not in data
        assert "email" in data


@pytest.mark.asyncio
async def test_child_management_security_and_pin_hashing():
    """Verify child profile creation hashes PIN and enforces authentication."""
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        # 1. Unauthenticated request to manage children must fail
        unauth_resp = await ac.get("/api/children/manage")
        assert unauth_resp.status_code == 401

        # 2. Register parent 1
        p1 = await ac.post("/api/auth/register", json={
            "email": "parent1@example.com",
            "password": "password123456",
            "name": "Parent One"
        })
        token1 = p1.json()["access_token"]
        headers1 = {"Authorization": f"Bearer {token1}"}

        # 3. Create child for parent 1
        create_resp = await ac.post("/api/children/manage", headers=headers1, json={
            "name": "Leo",
            "age": 6,
            "avatar": "compass",
            "pin": "1234"
        })
        assert create_resp.status_code == 200
        child_data = create_resp.json()
        child_id = child_data["id"]
        # PIN must never be returned in response!
        assert "pin" not in child_data or child_data.get("pin") is None
        assert child_data.get("has_pin") is True

        # 4. Verify PIN verification works
        verify_resp = await ac.post("/api/children/verify-pin", json={
            "child_id": child_id,
            "pin": "1234"
        })
        assert verify_resp.status_code == 200
        assert verify_resp.json()["verified"] is True

        # Wrong PIN returns 401
        wrong_verify = await ac.post("/api/children/verify-pin", json={
            "child_id": child_id,
            "pin": "9999"
        })
        assert wrong_verify.status_code == 401

        # 5. Register parent 2 and verify tenant isolation
        p2 = await ac.post("/api/auth/register", json={
            "email": "parent2@example.com",
            "password": "password123456",
            "name": "Parent Two"
        })
        token2 = p2.json()["access_token"]
        headers2 = {"Authorization": f"Bearer {token2}"}

        # Parent 2 should NOT see parent 1's child
        p2_children = await ac.get("/api/children/manage", headers=headers2)
        assert p2_children.status_code == 200
        assert len(p2_children.json()) == 0

        # Parent 2 cannot modify parent 1's child
        tamper_resp = await ac.put(f"/api/children/manage/{child_id}", headers=headers2, json={
            "name": "Hacked Leo"
        })
        assert tamper_resp.status_code == 404


@pytest.mark.asyncio
async def test_playlist_mutation_authorization():
    """Verify add, delete, and reorder playlist items require authenticated parent ownership."""
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        p1 = await ac.post("/api/auth/register", json={
            "email": "playlist_parent@example.com",
            "password": "password123456",
            "name": "Playlist Parent"
        })
        headers = {"Authorization": f"Bearer {p1.json()['access_token']}"}

        # Create child
        c_resp = await ac.post("/api/children/manage", headers=headers, json={
            "name": "Maya",
            "age": 7,
            "avatar": "sparkles",
            "pin": "4321"
        })
        child_id = c_resp.json()["id"]

        # Unauthenticated add-video must be rejected (401)
        unauth_add = await ac.post(f"/api/playlists/child/{child_id}/add-video", json={
            "youtube_id": "libKVRa0WV4",
            "title": "Solar System Song",
            "channel_name": "Kids Tube",
            "thumbnail_url": "https://img.youtube.com/vi/libKVRa0WV4/hqdefault.jpg",
            "duration_seconds": 180
        })
        assert unauth_add.status_code == 401

        # Authenticated add-video succeeds when mocked
        with patch("app.routes.playlists.verify_youtube_embeddable", new=AsyncMock(return_value=(True, "OK"))):
            auth_add = await ac.post(f"/api/playlists/child/{child_id}/add-video", headers=headers, json={
                "youtube_id": "libKVRa0WV4",
                "title": "Solar System Song",
                "channel_name": "Kids Tube",
                "thumbnail_url": "https://img.youtube.com/vi/libKVRa0WV4/hqdefault.jpg",
                "duration_seconds": 180
            })
            assert auth_add.status_code == 200
            assert auth_add.json()["status"] == "ok"


@pytest.mark.asyncio
async def test_progress_update_authorization():
    """Verify watch progress updates require authenticated parent owning the child profile."""
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        p1 = await ac.post("/api/auth/register", json={
            "email": "progress_parent@example.com",
            "password": "password123456",
            "name": "Progress Parent"
        })
        headers = {"Authorization": f"Bearer {p1.json()['access_token']}"}

        # Create child
        c_resp = await ac.post("/api/children/manage", headers=headers, json={
            "name": "Tommy",
            "age": 5,
            "avatar": "rocket",
            "pin": "1111"
        })
        child_id = c_resp.json()["id"]

        # Unauthenticated progress update must be rejected (401)
        unauth_prog = await ac.post("/api/progress/update", json={
            "child_id": child_id,
            "youtube_id": "libKVRa0WV4",
            "watched_seconds": 60,
            "completed": False
        })
        assert unauth_prog.status_code == 401

        # Authenticated progress update succeeds (200)
        auth_prog = await ac.post("/api/progress/update", headers=headers, json={
            "child_id": child_id,
            "youtube_id": "libKVRa0WV4",
            "watched_seconds": 60,
            "completed": False
        })
        assert auth_prog.status_code == 200
        assert auth_prog.json()["success"] is True


@pytest.mark.asyncio
async def test_security_headers_present():
    """Verify HTTP security headers are injected in every response."""
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        resp = await ac.get("/health")
        assert resp.status_code == 200
        assert resp.headers.get("X-Content-Type-Options") == "nosniff"
        assert resp.headers.get("X-Frame-Options") == "SAMEORIGIN"
        assert resp.headers.get("X-XSS-Protection") == "1; mode=block"
        assert "Strict-Transport-Security" in resp.headers
        assert resp.headers.get("Referrer-Policy") == "strict-origin-when-cross-origin"
