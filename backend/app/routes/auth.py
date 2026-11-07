import secrets
from datetime import datetime, timedelta, timezone
from typing import Dict, Any, Optional
from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from pydantic import BaseModel, EmailStr, ConfigDict
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
    password: str


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
    reset_code: str


class ResetPasswordRequest(BaseModel):
    email: EmailStr
    code: str
    new_password: str


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


@router.post("/forgot-password", response_model=ForgotPasswordResponse)
async def forgot_password(data: ForgotPasswordRequest, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(User).where(User.email == data.email.lower().strip()))
    user = result.scalar_one_or_none()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="No parent account registered with this email address.",
        )

    # Generate a secure 6-digit numeric verification code
    code = f"{secrets.randbelow(900000) + 100000}"
    expires_at = datetime.now(timezone.utc) + timedelta(minutes=15)
    PASSWORD_RESET_STORE[data.email.lower().strip()] = {
        "code": code,
        "expires_at": expires_at,
    }

    return ForgotPasswordResponse(
        message="A 6-digit verification code has been generated. Use it to reset your password.",
        email=data.email,
        reset_code=code,
    )


@router.post("/reset-password")
async def reset_password(data: ResetPasswordRequest, db: AsyncSession = Depends(get_db)):
    email_key = data.email.lower().strip()
    entry = PASSWORD_RESET_STORE.get(email_key)

    if not entry:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="No active reset code found for this email. Please request a new code.",
        )

    if datetime.now(timezone.utc) > entry["expires_at"]:
        del PASSWORD_RESET_STORE[email_key]
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="The verification code has expired. Please request a new one.",
        )

    if entry["code"] != data.code.strip():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid verification code. Please check and try again.",
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

    # Invalidate code
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

