import secrets
from datetime import datetime, timedelta, timezone
from typing import Dict, Any, Optional
from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from pydantic import BaseModel, EmailStr, ConfigDict, Field
from app.config import settings
from app.database import get_db
from app.models import User
from app.auth import hash_password, verify_password, create_access_token, get_current_user

router = APIRouter(prefix="/api/auth", tags=["Authentication"])

# In-memory store for password reset verification codes
# Key: email (lowercased), Value: {"code": "123456", "expires_at": datetime}
PASSWORD_RESET_STORE: Dict[str, Dict[str, Any]] = {}


class RegisterRequest(BaseModel):
    name: str
    email: EmailStr
    password: str = Field(..., min_length=8, description="Minimum 8 characters")


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user_id: str
    name: str
    email: str


class UserResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    id: str
    name: str
    email: str


class UpdateProfileRequest(BaseModel):
    name: Optional[str] = None
    email: Optional[EmailStr] = None
    current_password: Optional[str] = None
    new_password: Optional[str] = None


class UpdateProfileResponse(BaseModel):
    id: str
    name: str
    email: str
    message: str = "Profile updated successfully."
    access_token: Optional[str] = None


class ForgotPasswordRequest(BaseModel):
    email: EmailStr


class ForgotPasswordResponse(BaseModel):
    message: str
    email: str


class ResetPasswordRequest(BaseModel):
    email: EmailStr
    code: str
    new_password: str = Field(..., min_length=8, description="Minimum 8 characters")


class VerifyParentPasswordRequest(BaseModel):
    email: EmailStr
    password: str


@router.post("/register", response_model=TokenResponse, status_code=status.HTTP_201_CREATED)
async def register(data: RegisterRequest, db: AsyncSession = Depends(get_db)):
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

    token = create_access_token({"sub": str(user.id)})
    return TokenResponse(
        access_token=token,
        user_id=str(user.id),
        name=user.name,
        email=user.email,
    )


@router.post("/login", response_model=TokenResponse)
async def login(
    form_data: OAuth2PasswordRequestForm = Depends(),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(select(User).where(User.email == form_data.username))
    user = result.scalar_one_or_none()
    if not user or not verify_password(form_data.password, user.password_hash):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect email or password",
        )

    token = create_access_token({"sub": str(user.id)})
    return TokenResponse(
        access_token=token,
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
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """
    Allow authenticated parent to update their name, email, or password.
    Password update requires verifying the current password.
    Email change verifies uniqueness across other accounts.
    """
    # 1. Update Name
    if data.name is not None:
        clean_name = data.name.strip()
        if len(clean_name) < 2:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Name must be at least 2 characters long.",
            )
        current_user.name = clean_name

    # 2. Update Email
    if data.email is not None:
        clean_email = data.email.lower().strip()
        if clean_email != current_user.email:
            existing = await db.execute(
                select(User).where(User.email == clean_email, User.id != current_user.id)
            )
            if existing.scalar_one_or_none():
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="An account with this email address already exists.",
                )
            current_user.email = clean_email

    # 3. Update Password
    if data.new_password:
        if not data.current_password:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Current password is required to set a new password.",
            )
        if not verify_password(data.current_password, current_user.password_hash):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Current password does not match.",
            )
        if len(data.new_password) < 6:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="New password must be at least 6 characters long.",
            )
        current_user.password_hash = hash_password(data.new_password)

    await db.commit()
    await db.refresh(current_user)

    # Issue a fresh JWT token with the latest profile details
    new_token = create_access_token({"sub": str(current_user.id)})

    return UpdateProfileResponse(
        id=str(current_user.id),
        name=current_user.name,
        email=current_user.email,
        message="Profile details updated successfully.",
        access_token=new_token,
    )


@router.delete("/account")
async def delete_account(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """
    Permanently delete the authenticated parent's account and all associated
    child profiles, playlists, and watch history.
    Complies with Google Play Data Safety, GDPR, and COPPA account deletion mandates.
    """
    user_email = current_user.email
    await db.delete(current_user)
    await db.commit()
    return {
        "status": "ok",
        "message": f"Account for {user_email} and all associated child profiles have been permanently deleted.",
    }


class GoogleLoginRequest(BaseModel):
    id_token: str


@router.post("/google", response_model=TokenResponse)
async def google_login(data: GoogleLoginRequest, db: AsyncSession = Depends(get_db)):
    """Authenticate or register a parent account using a Google ID token with audience verification."""
    from google.oauth2 import id_token
    from google.auth.transport import requests
    import logging
    logger = logging.getLogger("uvicorn.error")

    try:
        target_audience = getattr(settings, "GOOGLE_CLIENT_ID", None) or None
        id_info = id_token.verify_oauth2_token(data.id_token, requests.Request(), audience=target_audience)
        
        # Verify issuer
        if id_info.get("iss") not in ["accounts.google.com", "https://accounts.google.com"]:
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token issuer")

        email = id_info.get("email", "").lower().strip()
        email_verified = id_info.get("email_verified", False)
        if not email or not email_verified:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Verified Google email required")
        name = id_info.get("name") or (email.split("@")[0] if email else "Parent")
    except HTTPException:
        raise
    except Exception as e:
        logger.warning(f"Google token verification failed: {e}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Google authentication failed. Please check your credentials."
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

    token = create_access_token({"sub": str(user.id), "email": user.email})
    return TokenResponse(
        access_token=token,
        user_id=str(user.id),
        name=user.name,
        email=user.email,
    )


@router.post("/forgot-password", response_model=ForgotPasswordResponse)
async def forgot_password(data: ForgotPasswordRequest, db: AsyncSession = Depends(get_db)):
    clean_email = data.email.lower().strip()
    result = await db.execute(select(User).where(User.email == clean_email))
    user = result.scalar_one_or_none()
    
    # Generic uniform response for registered and unregistered emails to prevent email enumeration
    generic_response = ForgotPasswordResponse(
        message="If this email is registered, a 6-digit verification code has been generated. Use it to reset your password.",
        email=data.email,
    )
    
    if not user:
        return generic_response

    # Generate a cryptographically secure 6-digit numeric verification code
    code = f"{secrets.randbelow(900000) + 100000}"
    expires_at = datetime.now(timezone.utc) + timedelta(minutes=15)
    
    # Store bcrypt hash of code with attempt counter for brute-force protection
    PASSWORD_RESET_STORE[clean_email] = {
        "code_hash": hash_password(code),
        "expires_at": expires_at,
        "attempts": 0,
        "created_at": datetime.now(timezone.utc),
    }
    
    # Security: never log the raw verification code in server logs
    import logging
    logging.getLogger("uvicorn.error").info(f"Password reset verification initiated for {clean_email}")

    return generic_response


@router.post("/reset-password")
async def reset_password(data: ResetPasswordRequest, db: AsyncSession = Depends(get_db)):
    email_key = data.email.lower().strip()
    entry = PASSWORD_RESET_STORE.get(email_key)

    if not entry:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="No active reset request found for this email. Please request a new code.",
        )

    # Check expiration
    if datetime.now(timezone.utc) > entry["expires_at"]:
        del PASSWORD_RESET_STORE[email_key]
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="The verification code has expired. Please request a new one.",
        )

    # Brute-force attempt limiting (max 5 guesses)
    entry["attempts"] += 1
    if entry["attempts"] > 5:
        del PASSWORD_RESET_STORE[email_key]
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail="Too many incorrect verification attempts. This code is invalidated. Please request a new one.",
        )

    # Verify code using constant-time bcrypt verification against stored hash
    if not verify_password(data.code.strip(), entry["code_hash"]):
        remaining = 5 - entry["attempts"]
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Invalid verification code. {remaining} attempt(s) remaining.",
        )

    if len(data.new_password) < 6:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="New password must be at least 6 characters long.",
        )

    result = await db.execute(select(User).where(User.email == email_key))
    user = result.scalar_one_or_none()
    if not user:
        raise HTTPException(status_code=404, detail="User account not found.")

    user.password_hash = hash_password(data.new_password)
    await db.commit()

    # Invalidate code immediately upon use
    if email_key in PASSWORD_RESET_STORE:
        del PASSWORD_RESET_STORE[email_key]

    return {
        "status": "ok",
        "message": "Password successfully reset! You can now log in with your new password.",
    }


@router.post("/verify-parent-password")
async def verify_parent_password(data: VerifyParentPasswordRequest, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(User).where(User.email == data.email.lower().strip()))
    user = result.scalar_one_or_none()
    if not user or not verify_password(data.password, user.password_hash):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect parent account password.",
        )
    return {
        "verified": True,
        "message": "Parent account verified.",
        "user_id": str(user.id),
        "name": user.name,
    }

