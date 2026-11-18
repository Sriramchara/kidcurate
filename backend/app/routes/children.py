from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from pydantic import BaseModel, EmailStr, ConfigDict
from typing import Optional, List, Dict, Any
from datetime import datetime, timedelta, timezone
from app.database import get_db
from app.models import User, Child
from app.auth import get_current_user, get_optional_current_user, verify_password, hash_password
import uuid

router = APIRouter(prefix="/api/children", tags=["Children"])

# In-memory store for PIN attempt lockout (brute-force defense)
PIN_LOCKOUT_STORE: Dict[str, Dict[str, Any]] = {}

def normalize_avatar(avatar_val: Optional[str]) -> str:
    return str(avatar_val).strip() if avatar_val else "compass"


class ChildCreate(BaseModel):
    name: str
    age: int
    avatar: Optional[str] = "compass"
    pin: str  # 4-digit


class ChildUpdate(BaseModel):
    name: Optional[str] = None
    age: Optional[int] = None
    avatar: Optional[str] = None
    pin: Optional[str] = None


class ChildResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    id: str
    name: str
    age: int
    avatar: str
    parent_id: str


class ChildPINVerify(BaseModel):
    child_id: str
    pin: str


@router.get("/", response_model=List[ChildResponse])
async def list_children(
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    result = await db.execute(
        select(Child).where(Child.parent_id == current_user.id)
    )
    children = result.scalars().all()
    return [
        ChildResponse(id=str(c.id), name=c.name, age=c.age,
                      avatar=normalize_avatar(c.avatar), parent_id=str(c.parent_id))
        for c in children
    ]


@router.post("/", response_model=ChildResponse, status_code=status.HTTP_201_CREATED)
async def create_child(
    data: ChildCreate,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    if len(data.pin) != 4 or not data.pin.isdigit():
        raise HTTPException(status_code=400, detail="PIN must be exactly 4 digits")
    if not (1 <= data.age <= 18):
        raise HTTPException(status_code=400, detail="Age must be between 1 and 18")

    child = Child(
        parent_id=current_user.id,
        name=data.name,
        age=data.age,
        avatar=normalize_avatar(data.avatar or "compass"),
        pin=data.pin,
    )
    db.add(child)
    await db.commit()
    await db.refresh(child)

    from app.models import Playlist
    starter_playlist = Playlist(
        id=str(uuid.uuid4()),
        child_id=str(child.id),
        title=f"{child.name}'s Adventure Playlist",
        is_active=True,
        total_duration_seconds=0,
    )
    db.add(starter_playlist)
    await db.commit()
    return ChildResponse(
        id=str(child.id), name=child.name, age=child.age,
        avatar=normalize_avatar(child.avatar), parent_id=str(child.parent_id)
    )


class ChildPublicProfile(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    id: str
    name: str
    avatar: str


class ChildManageResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    id: str
    name: str
    age: int
    avatar: str
    pin: Optional[str] = "••••"
    has_pin: bool = True
    parent_id: Optional[str] = None


@router.get("/manage", response_model=List[ChildManageResponse])
async def list_manage_children(
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Used by Parent Zone to list children with full details. Scoped strictly to parent."""
    result = await db.execute(select(Child).where(Child.parent_id == current_user.id))
    children = result.scalars().all()
    return [
        ChildManageResponse(
            id=str(c.id),
            name=c.name,
            age=c.age,
            avatar=normalize_avatar(c.avatar),
            pin=(c.pin if (c.pin and not c.pin.startswith("$2")) else None),
            has_pin=bool(c.pin),
            parent_id=str(c.parent_id) if c.parent_id else None
        )
        for c in children
    ]


@router.post("/manage", response_model=ChildManageResponse)
async def create_manage_child(
    data: ChildCreate,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Used by Parent Zone to create a new child profile."""
    if len(data.pin) != 4 or not data.pin.isdigit():
        raise HTTPException(status_code=400, detail="PIN must be exactly 4 digits")
    if not (1 <= data.age <= 18):
        raise HTTPException(status_code=400, detail="Age must be between 1 and 18")

    parent_id = str(current_user.id)

    child = Child(
        id=str(uuid.uuid4()),
        parent_id=parent_id,
        name=data.name.strip(),
        age=data.age,
        avatar=normalize_avatar(data.avatar or "compass"),
        pin=data.pin,
    )
    db.add(child)
    await db.commit()
    await db.refresh(child)

    # Initialize a default starter playlist so today's playlist is always ready
    from app.models import Playlist
    starter_playlist = Playlist(
        id=str(uuid.uuid4()),
        child_id=str(child.id),
        title=f"{child.name}'s Adventure Playlist",
        is_active=True,
        total_duration_seconds=0,
    )
    db.add(starter_playlist)
    await db.commit()

    return ChildManageResponse(
        id=str(child.id),
        name=child.name,
        age=child.age,
        avatar=normalize_avatar(child.avatar),
        pin=None,
        has_pin=True,
        parent_id=str(child.parent_id)
    )


@router.put("/manage/{child_id}", response_model=ChildManageResponse)
async def update_manage_child(
    child_id: str,
    data: ChildUpdate,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Used by Parent Zone to update child name, age, avatar, or pin. Strictly scoped to parent."""
    result = await db.execute(
        select(Child).where(Child.id == child_id, Child.parent_id == current_user.id)
    )
    child = result.scalar_one_or_none()
    if not child:
        raise HTTPException(status_code=404, detail="Child profile not found or access denied")

    if data.name is not None and data.name.strip():
        child.name = data.name.strip()
    if data.age is not None:
        if not (1 <= data.age <= 18):
            raise HTTPException(status_code=400, detail="Age must be between 1 and 18")
        child.age = data.age
    if data.avatar is not None:
        child.avatar = normalize_avatar(data.avatar)
    if data.pin is not None and data.pin.strip() and data.pin != "••••":
        if len(data.pin) != 4 or not data.pin.isdigit():
            raise HTTPException(status_code=400, detail="PIN must be exactly 4 digits")
        child.pin = data.pin

    await db.commit()
    await db.refresh(child)
    return ChildManageResponse(
        id=str(child.id),
        name=child.name,
        age=child.age,
        avatar=normalize_avatar(child.avatar),
        pin=None,
        has_pin=bool(child.pin),
        parent_id=str(child.parent_id)
    )


@router.delete("/manage/{child_id}")
async def delete_manage_child(
    child_id: str,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Used by Parent Zone to delete a child profile. Strictly scoped to parent."""
    from app.models import Playlist, PlaylistItem
    result = await db.execute(
        select(Child).where(Child.id == child_id, Child.parent_id == current_user.id)
    )
    child = result.scalar_one_or_none()
    if not child:
        raise HTTPException(status_code=404, detail="Child profile not found or access denied")

    result_pl = await db.execute(select(Playlist).where(Playlist.child_id == child_id))
    playlists = result_pl.scalars().all()
    for pl in playlists:
        await db.refresh(pl, ["items"])
        for item in pl.items:
            await db.delete(item)
        await db.delete(pl)

    await db.delete(child)
    await db.commit()
    return {"status": "ok", "message": f"Child profile {child.name} deleted"}


@router.get("/profiles", response_model=List[ChildPublicProfile])
async def list_public_child_profiles(
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Used by Android app to display available children for login. Scoped strictly to authenticated parent."""
    result = await db.execute(select(Child).where(Child.parent_id == current_user.id))
    children = result.scalars().all()
    return [
        ChildPublicProfile(id=str(c.id), name=c.name, avatar=normalize_avatar(c.avatar))
        for c in children
    ]


@router.get("/{child_id}", response_model=ChildResponse)
async def get_child(
    child_id: str,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    result = await db.execute(
        select(Child).where(Child.id == child_id, Child.parent_id == current_user.id)
    )
    child = result.scalar_one_or_none()
    if not child:
        raise HTTPException(status_code=404, detail="Child not found")
    return ChildResponse(
        id=str(child.id), name=child.name, age=child.age,
        avatar=normalize_avatar(child.avatar), parent_id=str(child.parent_id)
    )


@router.put("/{child_id}", response_model=ChildResponse)
async def update_child(
    child_id: str,
    data: ChildUpdate,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    result = await db.execute(
        select(Child).where(Child.id == child_id, Child.parent_id == current_user.id)
    )
    child = result.scalar_one_or_none()
    if not child:
        raise HTTPException(status_code=404, detail="Child not found")

    if data.name is not None:
        child.name = data.name
    if data.age is not None:
        child.age = data.age
    if data.avatar is not None:
        child.avatar = normalize_avatar(data.avatar)
    if data.pin is not None:
        if len(data.pin) != 4 or not data.pin.isdigit():
            raise HTTPException(status_code=400, detail="PIN must be exactly 4 digits")
        child.pin = data.pin

    await db.commit()
    await db.refresh(child)
    return ChildResponse(
        id=str(child.id), name=child.name, age=child.age,
        avatar=normalize_avatar(child.avatar), parent_id=str(child.parent_id)
    )


@router.delete("/{child_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_child(
    child_id: str,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    result = await db.execute(
        select(Child).where(Child.id == child_id, Child.parent_id == current_user.id)
    )
    child = result.scalar_one_or_none()
    if not child:
        raise HTTPException(status_code=404, detail="Child not found")
    await db.delete(child)
    await db.commit()


@router.post("/verify-pin")
async def verify_child_pin(
    data: ChildPINVerify,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Verify a child PIN only inside the authenticated account that owns the child."""
    cid = data.child_id
    lockout = PIN_LOCKOUT_STORE.get(cid)
    now = datetime.now(timezone.utc)
    if lockout:
        if now < lockout["locked_until"]:
            remaining = int((lockout["locked_until"] - now).total_seconds())
            raise HTTPException(
                status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                detail=f"Too many incorrect attempts. PIN entry locked for {remaining} seconds."
            )
        elif now >= lockout["locked_until"]:
            del PIN_LOCKOUT_STORE[cid]

    result = await db.execute(
        select(Child).where(Child.id == cid, Child.parent_id == current_user.id)
    )
    child = result.scalar_one_or_none()
    if not child:
        raise HTTPException(status_code=404, detail="Child not found")

    # Constant-time verification supporting both bcrypt hashes and legacy plaintext PINs
    is_valid = False
    if child.pin.startswith("$2b$") or child.pin.startswith("$2a$"):
        is_valid = verify_password(data.pin, child.pin)
    else:
        is_valid = (child.pin == data.pin)

    if not is_valid:
        entry = PIN_LOCKOUT_STORE.setdefault(cid, {"attempts": 0, "locked_until": now})
        entry["attempts"] += 1
        if entry["attempts"] >= 5:
            entry["locked_until"] = now + timedelta(minutes=5)
            raise HTTPException(
                status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                detail="Too many incorrect attempts. PIN entry locked for 5 minutes."
            )
        remaining_attempts = 5 - entry["attempts"]
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Incorrect PIN. {remaining_attempts} attempt(s) remaining."
        )

    # Success: clear failed attempts
    if cid in PIN_LOCKOUT_STORE:
        del PIN_LOCKOUT_STORE[cid]

    return {
        "verified": True,
        "child_id": str(child.id),
        "name": child.name,
        "avatar": normalize_avatar(child.avatar),
    }


class ChildPINReset(BaseModel):
    child_id: str
    new_pin: str
    parent_email: Optional[EmailStr] = None
    parent_password: Optional[str] = None


@router.post("/reset-pin")
async def reset_child_pin(
    data: ChildPINReset,
    db: AsyncSession = Depends(get_db),
    current_user: Optional[User] = Depends(get_optional_current_user),
):
    """Allows parents to securely reset a child's PIN by verifying parent credentials and enforcing ownership.
    Supports either an active parent session (e.g. Google Sign-In) or parent email/password.
    """
    if len(data.new_pin) != 4 or not data.new_pin.isdigit():
        raise HTTPException(status_code=400, detail="New PIN must be exactly 4 digits.")

    parent_id = None
    if current_user:
        parent_id = current_user.id
    elif data.parent_email and data.parent_password:
        result_u = await db.execute(select(User).where(User.email == data.parent_email.lower().strip()))
        parent = result_u.scalar_one_or_none()
        if not parent or not verify_password(data.parent_password, parent.password_hash):
            raise HTTPException(status_code=401, detail="Incorrect parent password.")
        parent_id = parent.id
    else:
        raise HTTPException(
            status_code=401,
            detail="Parent authentication or parent password is required to reset child PIN.",
        )

    # Strictly enforce child.parent_id == parent_id to prevent cross-account PIN tampering
    result_c = await db.execute(
        select(Child).where(Child.id == data.child_id, Child.parent_id == parent_id)
    )
    child = result_c.scalar_one_or_none()
    if not child:
        raise HTTPException(status_code=404, detail="Child profile not found or does not belong to your account.")

    child.pin = data.new_pin
    await db.commit()
    return {
        "status": "ok",
        "message": f"PIN for {child.name} has been successfully reset.",
        "child_id": str(child.id),
    }
