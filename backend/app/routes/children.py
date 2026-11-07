from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from pydantic import BaseModel, EmailStr, ConfigDict
from typing import Optional, List
from app.database import get_db
from app.models import User, Child
from app.auth import get_current_user, get_optional_current_user, verify_password
import uuid

router = APIRouter(prefix="/api/children", tags=["Children"])

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
    pin: str
    parent_id: Optional[str] = None


@router.get("/manage", response_model=List[ChildManageResponse])
async def list_manage_children(
    db: AsyncSession = Depends(get_db),
    current_user: Optional[User] = Depends(get_optional_current_user)
):
    """Used by Parent Zone to list children with full details (name, age, avatar, pin)."""
    if current_user:
        result = await db.execute(select(Child).where(Child.parent_id == current_user.id))
    else:
        result = await db.execute(select(Child))
    children = result.scalars().all()
    return [
        ChildManageResponse(
            id=str(c.id),
            name=c.name,
            age=c.age,
            avatar=normalize_avatar(c.avatar),
            pin=c.pin,
            parent_id=str(c.parent_id) if c.parent_id else None
        )
        for c in children
    ]


@router.post("/manage", response_model=ChildManageResponse)
async def create_manage_child(
    data: ChildCreate,
    db: AsyncSession = Depends(get_db),
    current_user: Optional[User] = Depends(get_optional_current_user)
):
    """Used by Parent Zone to create a new child profile."""
    if len(data.pin) != 4 or not data.pin.isdigit():
        raise HTTPException(status_code=400, detail="PIN must be exactly 4 digits")
    if not (1 <= data.age <= 18):
        raise HTTPException(status_code=400, detail="Age must be between 1 and 18")

    if current_user:
        parent_id = str(current_user.id)
    else:
        result = await db.execute(select(User).limit(1))
        parent = result.scalar_one_or_none()
        parent_id = str(parent.id) if parent else str(uuid.uuid4())

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
    return ChildManageResponse(
        id=str(child.id),
        name=child.name,
        age=child.age,
        avatar=normalize_avatar(child.avatar),
        pin=child.pin,
        parent_id=str(child.parent_id)
    )


@router.put("/manage/{child_id}", response_model=ChildManageResponse)
async def update_manage_child(child_id: str, data: ChildUpdate, db: AsyncSession = Depends(get_db)):
    """Used by Parent Zone to update child name, age, avatar, or pin."""
    result = await db.execute(select(Child).where(Child.id == child_id))
    child = result.scalar_one_or_none()
    if not child:
        raise HTTPException(status_code=404, detail="Child profile not found")

    if data.name is not None and data.name.strip():
        child.name = data.name.strip()
    if data.age is not None:
        if not (1 <= data.age <= 18):
            raise HTTPException(status_code=400, detail="Age must be between 1 and 18")
        child.age = data.age
    if data.avatar is not None:
        child.avatar = normalize_avatar(data.avatar)
    if data.pin is not None:
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
        pin=child.pin,
        parent_id=str(child.parent_id)
    )


@router.delete("/manage/{child_id}")
async def delete_manage_child(child_id: str, db: AsyncSession = Depends(get_db)):
    """Used by Parent Zone to delete a child profile."""
    from app.models import Playlist, PlaylistItem
    result = await db.execute(select(Child).where(Child.id == child_id))
    child = result.scalar_one_or_none()
    if not child:
        raise HTTPException(status_code=404, detail="Child profile not found")

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
    current_user: Optional[User] = Depends(get_optional_current_user)
):
    """Used by Android app to display available children for login. Scoped to parent when authenticated."""
    if current_user:
        result = await db.execute(select(Child).where(Child.parent_id == current_user.id))
    else:
        result = await db.execute(select(Child))
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
async def verify_child_pin(data: ChildPINVerify, db: AsyncSession = Depends(get_db)):
    """Used by Android app to verify child PIN before showing their playlist."""
    result = await db.execute(
        select(Child).where(Child.id == data.child_id)
    )
    child = result.scalar_one_or_none()
    if not child:
        raise HTTPException(status_code=404, detail="Child not found")
    if child.pin != data.pin:
        raise HTTPException(status_code=401, detail="Incorrect PIN")
    return {
        "verified": True,
        "child_id": str(child.id),
        "name": child.name,
        "avatar": normalize_avatar(child.avatar),
    }


class ChildPINReset(BaseModel):
    child_id: str
    parent_email: EmailStr
    parent_password: str
    new_pin: str


@router.post("/reset-pin")
async def reset_child_pin(data: ChildPINReset, db: AsyncSession = Depends(get_db)):
    """Allows parents to securely reset a child's PIN by verifying parent credentials."""
    result_u = await db.execute(select(User).where(User.email == data.parent_email.lower().strip()))
    parent = result_u.scalar_one_or_none()
    if not parent or not verify_password(data.parent_password, parent.password_hash):
        raise HTTPException(status_code=401, detail="Incorrect parent password.")

    if len(data.new_pin) != 4 or not data.new_pin.isdigit():
        raise HTTPException(status_code=400, detail="New PIN must be exactly 4 digits.")

    result_c = await db.execute(select(Child).where(Child.id == data.child_id))
    child = result_c.scalar_one_or_none()
    if not child:
        raise HTTPException(status_code=404, detail="Child profile not found.")

    child.pin = data.new_pin
    await db.commit()
    return {
        "status": "ok",
        "message": f"PIN for {child.name} has been successfully reset.",
        "child_id": str(child.id),
    }
