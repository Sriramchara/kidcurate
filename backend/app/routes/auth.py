import secrets
import jwt
from datetime import datetime, timedelta, timezone
from typing import Optional
from fastapi import APIRouter, Depends, HTTPException, status, Response, Request, BackgroundTasks
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, delete
from pydantic import BaseModel, EmailStr, ConfigDict, Field
from app.config import settings
from app.database import get_db
from app.models import User, PasswordResetToken
from app.auth import (
    hash_password,
    verify_password,
    create_access_token,
    create_refresh_token,
    get_current_user,
    get_optional_current_user,
)
from app.services.email import send_password_reset_email

router = APIRouter(prefix="/api/auth", tags=["Authentication"])

AUTH_LOCKOUT_STORE: dict[str, dict] = {}


def check_auth_rate_limit(key: str, max_attempts: int = 5, lock_minutes: int = 5):
    now = datetime.now(timezone.utc)
    entry = AUTH_LOCKOUT_STORE.get(key)
    if entry:
        if now < entry["locked_until"]:
            remaining = int((entry["locked_until"] - now).total_seconds())
            raise HTTPException(
                status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                detail=f"Too many failed attempts. Temporarily locked for {remaining} seconds.",
            )
        elif now >= entry["locked_until"]:
            AUTH_LOCKOUT_STORE.pop(key, None)


def record_auth_failure(key: str, max_attempts: int = 5, lock_minutes: int = 5) -> int:
    now = datetime.now(timezone.utc)
    entry = AUTH_LOCKOUT_STORE.setdefault(key, {"attempts": 0, "locked_until": now})
    entry["attempts"] += 1
    if entry["attempts"] >= max_attempts:
        entry["locked_until"] = now + timedelta(minutes=lock_minutes)
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail=f"Too many failed attempts. Temporarily locked for {lock_minutes} minutes.",
        )
    return max_attempts - entry["attempts"]


def clear_auth_failures(key: str):
    AUTH_LOCKOUT_STORE.pop(key, None)


class RegisterRequest(BaseModel):
    name: str
    email: EmailStr
    password: str = Field(..., min_length=12, description="Minimum 12 characters")


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    refresh_token: Optional[str] = None
    user_id: str
    name: str
    email: str


class RefreshTokenRequest(BaseModel):
    refresh_token: Optional[str] = None


class UserResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    id: str
    name: str
    email: str


class UpdateProfileRequest(BaseModel):
    name: Optional[str] = None
    email: Optional[EmailStr] = None
    current_password: Optional[str] = None
    new_password: Optional[str] = Field(None, min_length=12, description="Minimum 12 characters")


class UpdateProfileResponse(BaseModel):
    id: str
    name: str
    email: str
    message: str = "Profile updated successfully."
    access_token: Optional[str] = None
    refresh_token: Optional[str] = None


class ForgotPasswordRequest(BaseModel):
    email: EmailStr


class ForgotPasswordResponse(BaseModel):
    message: str
    email: str


class ResetPasswordRequest(BaseModel):
    email: EmailStr
    code: str
    new_password: str = Field(..., min_length=12, description="Minimum 12 characters")


class VerifyParentPasswordRequest(BaseModel):
    email: EmailStr
    password: str


def set_auth_cookies(response: Response, access_token: str, refresh_token: str) -> None:
    """Helper to attach secure, HttpOnly authentication cookies."""
    response.set_cookie(
        key="access_token",
        value=access_token,
        httponly=True,
        secure=not settings.DEBUG,
        samesite="lax",
        max_age=settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60,
    )
    response.set_cookie(
        key="refresh_token",
        value=refresh_token,
        httponly=True,
        secure=not settings.DEBUG,
        samesite="lax",
        max_age=settings.REFRESH_TOKEN_EXPIRE_MINUTES * 60,
    )


@router.post("/register", response_model=TokenResponse, status_code=status.HTTP_201_CREATED)
async def register(
    data: RegisterRequest,
    response: Response,
    db: AsyncSession = Depends(get_db),
):
    # Check if email already exists
    result = await db.execute(select(User).where(User.email == data.email))
    if result.scalar_one_or_none():
        raise HTTPException(status_code=400, detail="Email already registered")

    user = User(
        name=data.name,
        email=data.email,
        password_hash=hash_password(data.password),
    )
    db.add(user)
    await db.commit()
    await db.refresh(user)

    access_token = create_access_token({"sub": str(user.id)})
    refresh_token = create_refresh_token({"sub": str(user.id)})
    set_auth_cookies(response, access_token, refresh_token)

    return TokenResponse(
        access_token=access_token,
        refresh_token=refresh_token,
        user_id=str(user.id),
        name=user.name,
        email=user.email,
    )


@router.post("/login", response_model=TokenResponse)
async def login(
    response: Response,
    form_data: OAuth2PasswordRequestForm = Depends(),
    db: AsyncSession = Depends(get_db),
):
    clean_email = form_data.username.lower().strip()
    check_auth_rate_limit(clean_email, max_attempts=5, lock_minutes=5)

    result = await db.execute(select(User).where(User.email == clean_email))
    user = result.scalar_one_or_none()
    if not user or not verify_password(form_data.password, user.password_hash):
        remaining = record_auth_failure(clean_email, max_attempts=5, lock_minutes=5)
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Incorrect email or password. {remaining} attempt(s) remaining.",
        )

    clear_auth_failures(clean_email)
    access_token = create_access_token({"sub": str(user.id)})
    refresh_token = create_refresh_token({"sub": str(user.id)})
    set_auth_cookies(response, access_token, refresh_token)

    return TokenResponse(
        access_token=access_token,
        refresh_token=refresh_token,
        user_id=str(user.id),
        name=user.name,
        email=user.email,
    )


@router.post("/refresh", response_model=TokenResponse)
async def refresh_access_token(
    request: Request,
    response: Response,
    data: Optional[RefreshTokenRequest] = None,
    db: AsyncSession = Depends(get_db),
):
    """Rotate access and refresh tokens using an existing valid refresh token."""
    raw_token = (data.refresh_token if data and data.refresh_token else None) or request.cookies.get("refresh_token")
    if not raw_token:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Refresh token required")

    try:
        payload = jwt.decode(raw_token, settings.SECRET_KEY, algorithms=[settings.ALGORITHM])
        if payload.get("type") != "refresh":
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token type")
        user_id = payload.get("sub")
        if not user_id:
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token subject")
    except Exception:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid or expired refresh token")

    result = await db.execute(select(User).where(User.id == user_id))
    user = result.scalar_one_or_none()
    if not user or not user.is_active:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="User not found or inactive")

    new_access_token = create_access_token({"sub": str(user.id)})
    new_refresh_token = create_refresh_token({"sub": str(user.id)})
    set_auth_cookies(response, new_access_token, new_refresh_token)

    return TokenResponse(
        access_token=new_access_token,
        refresh_token=new_refresh_token,
        user_id=str(user.id),
        name=user.name,
        email=user.email,
    )


@router.get("/me", response_model=UserResponse)
async def get_me(current_user: User = Depends(get_current_user)):
    return UserResponse(
        id=str(current_user.id),
        name=current_user.name,
        email=current_user.email,
    )


@router.put("/profile", response_model=UpdateProfileResponse)
async def update_profile(
    data: UpdateProfileRequest,
    response: Response,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    new_access_token = None
    new_refresh_token = None

    if data.name is not None and data.name.strip():
        current_user.name = data.name.strip()

    if data.email is not None and data.email != current_user.email:
        existing = await db.execute(select(User).where(User.email == data.email))
        if existing.scalar_one_or_none():
            raise HTTPException(status_code=400, detail="Email is already in use by another account.")
        current_user.email = data.email

    if data.new_password:
        if not data.current_password:
            raise HTTPException(
                status_code=400,
                detail="Current password is required to set a new password.",
            )
        if not verify_password(data.current_password, current_user.password_hash):
            raise HTTPException(
                status_code=400,
                detail="Current password is incorrect.",
            )
        if len(data.new_password) < 12:
            raise HTTPException(
                status_code=400,
                detail="New password must be at least 12 characters long.",
            )
        current_user.password_hash = hash_password(data.new_password)
        new_access_token = create_access_token({"sub": str(current_user.id)})
        new_refresh_token = create_refresh_token({"sub": str(current_user.id)})
        set_auth_cookies(response, new_access_token, new_refresh_token)

    await db.commit()
    await db.refresh(current_user)

    return UpdateProfileResponse(
        id=str(current_user.id),
        name=current_user.name,
        email=current_user.email,
        access_token=new_access_token,
        refresh_token=new_refresh_token,
    )


class GoogleLoginRequest(BaseModel):
    id_token: str
    email: Optional[EmailStr] = None
    name: Optional[str] = None


@router.post("/google", response_model=TokenResponse)
async def google_login(
    data: GoogleLoginRequest,
    response: Response,
    db: AsyncSession = Depends(get_db),
):
    """Authenticate or register a parent account using Google Sign-In."""
    import logging
    logger = logging.getLogger("uvicorn.error")

    email = None
    name = "Parent"
    target_audience = (settings.GOOGLE_CLIENT_ID or "179684236627-d7j0s15cvkj6li430jku6easqma5q81l.apps.googleusercontent.com").strip()

    if not data.id_token.strip():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="A Google ID token is required for authentication.",
        )

    from google.oauth2 import id_token
    from google.auth.transport import requests
    try:
        id_info = id_token.verify_oauth2_token(
            data.id_token,
            requests.Request(),
            audience=target_audience,
        )
        if id_info.get("iss") not in ["accounts.google.com", "https://accounts.google.com"]:
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token issuer")
        verified_email = id_info.get("email", "").lower().strip()
        email_verified = id_info.get("email_verified", False)
        if not verified_email or not email_verified:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Verified Google email required")
        email = verified_email
        name = id_info.get("name") or email.split("@")[0]
    except HTTPException:
        raise
    except Exception as e:
        logger.warning(f"Google token verification failed: {e}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Google authentication token verification failed.",
        )

    result = await db.execute(select(User).where(User.email == email))
    user = result.scalar_one_or_none()
    if not user:
        user = User(
            name=name,
            email=email,
            password_hash=hash_password(secrets.token_urlsafe(32)),
        )
        db.add(user)
        await db.commit()
        await db.refresh(user)
    elif name and name != "Parent" and (not user.name or user.name == "Parent" or user.name == email.split("@")[0]):
        user.name = name
        await db.commit()
        await db.refresh(user)

    access_token = create_access_token({"sub": str(user.id)})
    refresh_token = create_refresh_token({"sub": str(user.id)})
    set_auth_cookies(response, access_token, refresh_token)

    return TokenResponse(
        access_token=access_token,
        refresh_token=refresh_token,
        user_id=str(user.id),
        name=user.name,
        email=user.email,
    )


@router.post("/forgot-password", response_model=ForgotPasswordResponse)
async def forgot_password(
    data: ForgotPasswordRequest,
    background_tasks: BackgroundTasks,
    db: AsyncSession = Depends(get_db),
):
    clean_email = data.email.lower().strip()
    result = await db.execute(select(User).where(User.email == clean_email))
    user = result.scalar_one_or_none()
    
    # Generic uniform response for registered and unregistered emails to prevent email enumeration
    generic_response = ForgotPasswordResponse(
        message="If this email is registered, a 6-digit verification code has been sent. Use it to reset your password.",
        email=data.email,
    )
    
    if not user:
        return generic_response

    # Generate a cryptographically secure 6-digit numeric verification code
    code = f"{secrets.randbelow(900000) + 100000}"
    expires_at = datetime.now(timezone.utc) + timedelta(minutes=15)
    
    # Invalidate any prior active reset codes for this email
    await db.execute(delete(PasswordResetToken).where(PasswordResetToken.email == clean_email))

    # Persist hashed code in database with atomic attempt tracking and expiry
    reset_entry = PasswordResetToken(
        email=clean_email,
        code_hash=hash_password(code),
        expires_at=expires_at,
        attempts=0,
    )
    db.add(reset_entry)
    await db.commit()

    # Deliver verification code via email provider in background task
    background_tasks.add_task(send_password_reset_email, to_email=clean_email, code=code)

    import logging
    logging.getLogger("uvicorn.error").info(f"Password reset verification initiated for {clean_email}")

    return generic_response


@router.post("/reset-password")
async def reset_password(data: ResetPasswordRequest, db: AsyncSession = Depends(get_db)):
    email_key = data.email.lower().strip()

    # Query latest active reset token from database
    result = await db.execute(
        select(PasswordResetToken)
        .where(PasswordResetToken.email == email_key)
        .order_by(PasswordResetToken.created_at.desc())
    )
    entry = result.scalar_one_or_none()

    if not entry:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="No active reset request found for this email. Please request a new code.",
        )

    # Check expiration (ensure comparison with timezone-aware datetime)
    entry_expires = entry.expires_at if entry.expires_at.tzinfo else entry.expires_at.replace(tzinfo=timezone.utc)
    if datetime.now(timezone.utc) > entry_expires:
        await db.delete(entry)
        await db.commit()
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="The verification code has expired. Please request a new one.",
        )

    # Brute-force attempt limiting (max 5 guesses)
    entry.attempts += 1
    if entry.attempts > 5:
        await db.delete(entry)
        await db.commit()
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail="Too many incorrect verification attempts. This code is invalidated. Please request a new one.",
        )

    # Verify code using constant-time bcrypt verification against stored hash
    if not verify_password(data.code.strip(), entry.code_hash):
        await db.commit()
        remaining = 5 - entry.attempts
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Invalid verification code. {remaining} attempt(s) remaining.",
        )

    if len(data.new_password) < 12:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="New password must be at least 12 characters long.",
        )

    result = await db.execute(select(User).where(User.email == email_key))
    user = result.scalar_one_or_none()
    if not user:
        raise HTTPException(status_code=404, detail="User account not found.")

    user.password_hash = hash_password(data.new_password)
    # Invalidate token immediately upon successful password reset
    await db.delete(entry)
    await db.commit()

    return {
        "status": "ok",
        "message": "Password successfully reset! You can now log in with your new password.",
    }


@router.delete("/account")
async def delete_account(
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Permanently delete parent account and all associated children, playlists, and watch data."""
    from app.models import Child, Playlist, PlaylistItem, WatchProgress, CurationLog

    # Delete all curation audit logs for this parent
    res_logs = await db.execute(select(CurationLog).where(CurationLog.parent_id == current_user.id))
    for log_entry in res_logs.scalars().all():
        await db.delete(log_entry)

    res_children = await db.execute(select(Child).where(Child.parent_id == current_user.id))
    children = res_children.scalars().all()
    child_ids = [c.id for c in children]

    if child_ids:
        res_wp = await db.execute(select(WatchProgress).where(WatchProgress.child_id.in_(child_ids)))
        for wp in res_wp.scalars().all():
            await db.delete(wp)

        res_pl = await db.execute(select(Playlist).where(Playlist.child_id.in_(child_ids)))
        for pl in res_pl.scalars().all():
            res_items = await db.execute(select(PlaylistItem).where(PlaylistItem.playlist_id == pl.id))
            for item in res_items.scalars().all():
                await db.delete(item)
            await db.delete(pl)

        for c in children:
            await db.delete(c)

    res_tokens = await db.execute(select(PasswordResetToken).where(PasswordResetToken.email == current_user.email))
    for t in res_tokens.scalars().all():
        await db.delete(t)

    await db.delete(current_user)
    await db.commit()

    return {"status": "ok", "message": "Account and all associated cloud data deleted successfully."}


@router.get("/diagnostic/db-summary")
async def get_db_summary(
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Provides transparency on the parent's actual cloud database records."""
    from app.models import Child, Playlist, PlaylistItem, WatchProgress

    res_children = await db.execute(select(Child).where(Child.parent_id == current_user.id))
    children = res_children.scalars().all()
    child_ids = [c.id for c in children]

    playlist_count = 0
    item_count = 0
    watch_count = 0

    if child_ids:
        res_pl = await db.execute(select(Playlist).where(Playlist.child_id.in_(child_ids)))
        playlists = res_pl.scalars().all()
        playlist_count = len(playlists)
        p_ids = [p.id for p in playlists]
        if p_ids:
            res_items = await db.execute(select(PlaylistItem).where(PlaylistItem.playlist_id.in_(p_ids)))
            item_count = len(res_items.scalars().all())

        res_wp = await db.execute(select(WatchProgress).where(WatchProgress.child_id.in_(child_ids)))
        watch_count = len(res_wp.scalars().all())

    engine_name = "PostgreSQL" if "postgresql" in settings.DATABASE_URL.lower() else "SQLite"

    return {
        "parent_email": current_user.email,
        "database_engine": engine_name,
        "children_count": len(children),
        "playlists_count": playlist_count,
        "total_videos_count": item_count,
        "watch_history_count": watch_count,
        "account_created_at": current_user.created_at.isoformat() if current_user.created_at else "",
    }


@router.post("/verify-parent-password")
async def verify_parent_password(
    data: VerifyParentPasswordRequest,
    db: AsyncSession = Depends(get_db),
    current_user: Optional[User] = Depends(get_optional_current_user),
):
    clean_email = data.email.lower().strip()

    # If the user is already authenticated via Bearer token (e.g. Google Sign-In) and matches email
    if current_user and current_user.email.lower().strip() == clean_email:
        return {
            "verified": True,
            "message": "Parent account verified via active session.",
            "user_id": str(current_user.id),
            "name": current_user.name,
        }

    check_auth_rate_limit(f"pw_{clean_email}", max_attempts=5, lock_minutes=5)
    result = await db.execute(select(User).where(User.email == clean_email))
    user = result.scalar_one_or_none()
    if not user or not verify_password(data.password, user.password_hash):
        remaining = record_auth_failure(f"pw_{clean_email}", max_attempts=5, lock_minutes=5)
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Incorrect parent account password. {remaining} attempt(s) remaining.",
        )

    clear_auth_failures(f"pw_{clean_email}")
    return {
        "verified": True,
        "message": "Parent account verified.",
        "user_id": str(user.id),
        "name": user.name,
    }
