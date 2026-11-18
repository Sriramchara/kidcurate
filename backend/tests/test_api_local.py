import os
os.environ["DATABASE_URL"] = "sqlite+aiosqlite:///:memory:"
os.environ["ENVIRONMENT"] = "test"

import sys
from pathlib import Path

backend_dir = Path(__file__).resolve().parent.parent
if str(backend_dir) not in sys.path:
    sys.path.insert(0, str(backend_dir))

import pytest
import pytest_asyncio
import uuid
from unittest.mock import patch, AsyncMock
from httpx import AsyncClient, ASGITransport
from app.main import app
from app.database import engine, AsyncSessionLocal
from app.models import Base, User, Child
from app.config import Settings


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


def test_production_rejects_ephemeral_sqlite_database():
    with pytest.raises(ValueError, match="persistent PostgreSQL"):
        Settings(
            _env_file=None,
            ENVIRONMENT="production",
            DATABASE_URL="sqlite+aiosqlite:///kidcurate.db",
            SECRET_KEY="a-secure-production-secret-that-is-long-enough",
        )


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

        # Google login must never trust a caller-supplied email without a valid ID token.
        email_only = await ac.post("/api/auth/google", json={
            "email": "attacker@example.com",
            "name": "Attacker",
        })
        assert email_only.status_code == 422

        invalid_google_token = await ac.post("/api/auth/google", json={
            "id_token": "not-a-google-token",
            "email": "attacker@example.com",
            "name": "Attacker",
        })
        assert invalid_google_token.status_code == 401


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
        verify_resp = await ac.post("/api/children/verify-pin", headers=headers1, json={
            "child_id": child_id,
            "pin": "1234"
        })
        assert verify_resp.status_code == 200
        assert verify_resp.json()["verified"] is True

        # Wrong PIN returns 401
        wrong_verify = await ac.post("/api/children/verify-pin", headers=headers1, json={
            "child_id": child_id,
            "pin": "9999"
        })
        assert wrong_verify.status_code == 401

        async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as unauth_ac:
            unauth_profiles = await unauth_ac.get("/api/children/profiles")
            assert unauth_profiles.status_code == 401
            unauth_verify = await unauth_ac.post("/api/children/verify-pin", json={
                "child_id": child_id,
                "pin": "1234",
            })
            assert unauth_verify.status_code == 401

        # 5. Register parent 2 and verify tenant isolation
        p2 = await ac.post("/api/auth/register", json={
            "email": "parent2@example.com",
            "password": "password123456",
            "name": "Parent Two"
        })
        token2 = p2.json()["access_token"]
        headers2 = {"Authorization": f"Bearer {token2}"}

        cross_account_verify = await ac.post(
            "/api/children/verify-pin",
            headers=headers2,
            json={"child_id": child_id, "pin": "1234"},
        )
        assert cross_account_verify.status_code == 404

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
        async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as unauth_ac:
            unauth_add = await unauth_ac.post(f"/api/playlists/child/{child_id}/add-video", json={
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
        async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as unauth_ac:
            unauth_prog = await unauth_ac.post("/api/progress/update", json={
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


@pytest.mark.asyncio
async def test_password_minimum_length_enforced():
    """Verify passwords shorter than 12 characters are rejected."""
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        short_resp = await ac.post("/api/auth/register", json={
            "email": "shortpw@example.com",
            "password": "short8ch",
            "name": "Short PW"
        })
        assert short_resp.status_code == 422  # Pydantic validation failure


@pytest.mark.asyncio
async def test_token_refresh_flow():
    """Verify access and refresh token pair rotation."""
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        reg_resp = await ac.post("/api/auth/register", json={
            "email": "refreshtest@example.com",
            "password": "validpassword123",
            "name": "Refresh User"
        })
        assert reg_resp.status_code == 201
        data = reg_resp.json()
        assert "access_token" in data
        assert "refresh_token" in data
        assert data["refresh_token"] is not None

        # Refresh access token
        ref_resp = await ac.post("/api/auth/refresh", json={
            "refresh_token": data["refresh_token"]
        })
        assert ref_resp.status_code == 200
        ref_data = ref_resp.json()
        assert "access_token" in ref_data
        assert "refresh_token" in ref_data


@pytest.mark.asyncio
async def test_video_deduplication_and_delete_account():
    """Verify video deduplication and account deletion cascades cleanly."""
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        reg = await ac.post("/api/auth/register", json={
            "email": "lifecycle_parent@example.com",
            "password": "validpassword123",
            "name": "Lifecycle Parent"
        })
        assert reg.status_code == 201
        token = reg.json()["access_token"]
        headers = {"Authorization": f"Bearer {token}"}

        # Create child
        c_resp = await ac.post("/api/children/manage", headers=headers, json={
            "name": "Maya",
            "age": 6,
            "avatar": "star",
            "pin": "2222"
        })
        assert c_resp.status_code == 200
        child_id = c_resp.json()["id"]

        with patch("app.routes.playlists.verify_youtube_embeddable", return_value=(True, "")):
            # First addition
            add1 = await ac.post(f"/api/playlists/child/{child_id}/add-video", headers=headers, json={
                "youtube_id": "test_dedup_1",
                "title": "Dedup Test Video",
                "channel_name": "TestChannel",
                "thumbnail_url": "https://img.youtube.com/vi/test_dedup_1/hqdefault.jpg",
                "duration_seconds": 120
            })
            assert add1.status_code == 200
            assert add1.json()["status"] == "ok"

            # Second addition of identical video must be deduplicated
            add2 = await ac.post(f"/api/playlists/child/{child_id}/add-video", headers=headers, json={
                "youtube_id": "test_dedup_1",
                "title": "Dedup Test Video",
                "channel_name": "TestChannel",
                "thumbnail_url": "https://img.youtube.com/vi/test_dedup_1/hqdefault.jpg",
                "duration_seconds": 120
            })
            assert add2.status_code == 200
            assert add2.json()["status"] == "already_added"

        # Delete account
        del_resp = await ac.delete("/api/auth/account", headers=headers)
        assert del_resp.status_code == 200
        assert del_resp.json()["status"] == "ok"

        # Further authenticated requests must now fail (401)
        subsequent = await ac.get("/api/children/manage", headers=headers)
        assert subsequent.status_code == 401


@pytest.mark.asyncio
async def test_db_summary_diagnostic():
    """Verify database summary endpoint returns live table counts for parent."""
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        reg = await ac.post("/api/auth/register", json={
            "email": "dbsummary_test@example.com",
            "password": "validpassword123",
            "name": "DB Summary Parent"
        })
        assert reg.status_code == 201
        token = reg.json()["access_token"]
        headers = {"Authorization": f"Bearer {token}"}

        summary1 = await ac.get("/api/auth/diagnostic/db-summary", headers=headers)
        assert summary1.status_code == 200
        data1 = summary1.json()
        assert data1["parent_email"] == "dbsummary_test@example.com"
        assert data1["children_count"] == 0

        c_resp = await ac.post("/api/children/manage", headers=headers, json={
            "name": "Leo",
            "age": 5,
            "avatar": "rocket",
            "pin": "1234"
        })
        assert c_resp.status_code == 200

        summary2 = await ac.get("/api/auth/diagnostic/db-summary", headers=headers)
        assert summary2.status_code == 200
        data2 = summary2.json()
        assert data2["children_count"] == 1
        assert "database_engine" in data2


@pytest.mark.asyncio
async def test_token_type_enforcement():
    """Verify refresh tokens cannot be used as access tokens (SEC-002)."""
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        reg = await ac.post("/api/auth/register", json={
            "email": "tokentype_test@example.com",
            "password": "validpassword123",
            "name": "Token Type Test Parent"
        })
        assert reg.status_code == 201
        data = reg.json()
        access_token = data["access_token"]
        refresh_token = data["refresh_token"]

        # Valid access token succeeds
        res_valid = await ac.get("/api/children/manage", headers={"Authorization": f"Bearer {access_token}"})
        assert res_valid.status_code == 200

        # Refresh token used as bearer token MUST be rejected with 401
        res_invalid = await ac.get("/api/children/manage", headers={"Authorization": f"Bearer {refresh_token}"})
        assert res_invalid.status_code == 401


@pytest.mark.asyncio
async def test_apply_activity_pack_and_position_indexing():
    """Verify applying activity pack succeeds and correctly uses position index (BUG-001)."""
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        reg = await ac.post("/api/auth/register", json={
            "email": "pack_test@example.com",
            "password": "validpassword123",
            "name": "Pack Test Parent"
        })
        assert reg.status_code == 201
        headers = {"Authorization": f"Bearer {reg.json()['access_token']}"}

        c_resp = await ac.post("/api/children/manage", headers=headers, json={
            "name": "Pack Kid",
            "age": 6,
            "avatar": "sparkles",
            "pin": "5678"
        })
        assert c_resp.status_code == 200
        child_id = c_resp.json()["id"]

        packs_resp = await ac.get("/api/playlists/activity-packs")
        assert packs_resp.status_code == 200
        packs = packs_resp.json()
        assert len(packs) > 0
        pack_id = packs[0]["id"]

        apply_resp = await ac.post(f"/api/playlists/child/{child_id}/apply-pack/{pack_id}", headers=headers)
        assert apply_resp.status_code == 200
        assert apply_resp.json()["status"] == "ok"


@pytest.mark.asyncio
async def test_curation_history_endpoint():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        email = f"curate_hist_{uuid.uuid4().hex[:6]}@example.com"
        reg = await ac.post("/api/auth/register", json={
            "email": email,
            "password": "Password123!",
            "name": "Audit Parent"
        })
        assert reg.status_code == 201
        headers = {"Authorization": f"Bearer {reg.json()['access_token']}"}

        # Create child
        c_resp = await ac.post("/api/children/manage", headers=headers, json={
            "name": "Audit Kid",
            "age": 6,
            "avatar": "star",
            "pin": "1234"
        })
        assert c_resp.status_code == 200
        child_id = c_resp.json()["id"]

        # 1. Add video directly with mock YouTube embed verification
        with patch("app.routes.playlists.verify_youtube_embeddable", new=AsyncMock(return_value=(True, "OK"))):
            add_resp = await ac.post(f"/api/playlists/child/{child_id}/add-video", headers=headers, json={
                "youtube_id": "_7jhd4-o9gU",
                "title": "Twinkle Twinkle Little Star",
                "channel_name": "Super Simple Songs",
                "thumbnail_url": "https://img.youtube.com/vi/_7jhd4-o9gU/hqdefault.jpg",
                "duration_seconds": 180
            })
            assert add_resp.status_code == 200

        # 2. Query curation history
        hist_resp = await ac.get(f"/api/playlists/curation-history?child_id={child_id}", headers=headers)
        assert hist_resp.status_code == 200
        data = hist_resp.json()
        assert "history" in data
        assert len(data["history"]) >= 1
        first_entry = data["history"][0]
        assert first_entry["youtube_id"] == "_7jhd4-o9gU"
        assert first_entry["title"] == "Twinkle Twinkle Little Star"
        assert first_entry["action"] == "added"
        assert first_entry["child_name"] == "Audit Kid"


@pytest.mark.asyncio
async def test_child_deletion_cascades_foreign_keys():
    """Verify deleting a child cascades and removes curation logs and playlists without ForeignKeyViolation."""
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        p1 = await ac.post("/api/auth/register", json={
            "email": "cascade_parent@example.com",
            "password": "password123456",
            "name": "Cascade Parent"
        })
        headers = {"Authorization": f"Bearer {p1.json()['access_token']}"}

        # Create child
        c_resp = await ac.post("/api/children/manage", headers=headers, json={
            "name": "Cascade Child",
            "age": 5,
            "avatar": "heart",
            "pin": "1111"
        })
        assert c_resp.status_code == 200
        child_id = c_resp.json()["id"]

        # Add video to create PlaylistItem & CurationLog
        with patch("app.routes.playlists.verify_youtube_embeddable", new=AsyncMock(return_value=(True, "OK"))):
            add_resp = await ac.post(f"/api/playlists/child/{child_id}/add-video", headers=headers, json={
                "youtube_id": "test_cascade_vid",
                "title": "Cascade Song",
                "channel_name": "Test Tube",
                "thumbnail_url": "https://img.youtube.com/vi/test_cascade_vid/hqdefault.jpg",
                "duration_seconds": 120
            })
            assert add_resp.status_code == 200

        # Delete child profile via /manage/{child_id}
        del_resp = await ac.delete(f"/api/children/manage/{child_id}", headers=headers)
        assert del_resp.status_code == 200
        assert del_resp.json()["status"] == "ok"


@pytest.mark.asyncio
async def test_search_query_length_boundary():
    """Verify search queries over 120 characters are rejected with 422 validation error."""
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        # 121 characters should fail
        long_query = "a" * 121
        resp_too_long = await ac.get(f"/api/videos/search?q={long_query}")
        assert resp_too_long.status_code == 422

        # 120 characters should be accepted
        valid_query = "a" * 120
        resp_valid = await ac.get(f"/api/videos/search?q={valid_query}")
        assert resp_valid.status_code == 200





