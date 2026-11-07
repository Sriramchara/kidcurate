import uuid
from datetime import datetime
from sqlalchemy import (
    Column, String, Integer, Boolean, DateTime,
    ForeignKey, Text, Date, Float
)
from sqlalchemy.types import TypeDecorator, CHAR
from sqlalchemy.dialects.postgresql import UUID as PG_UUID
from sqlalchemy.orm import relationship, DeclarativeBase
from sqlalchemy.sql import func


class GUID(TypeDecorator):
    """Platform-independent GUID/UUID type.
    Uses PostgreSQL native UUID if on PG, otherwise CHAR(36),
    and accepts both uuid.UUID objects and UUID hex strings seamlessly.
    """
    impl = CHAR(36)
    cache_ok = True

    def load_dialect_impl(self, dialect):
        if dialect.name == "postgresql":
            return dialect.type_descriptor(PG_UUID())
        return dialect.type_descriptor(CHAR(36))

    def process_bind_param(self, value, dialect):
        if value is None:
            return value
        if not isinstance(value, uuid.UUID):
            try:
                value = uuid.UUID(str(value))
            except ValueError:
                return str(value)
        return value if dialect.name == "postgresql" else str(value)

    def process_result_value(self, value, dialect):
        if value is None:
            return value
        if not isinstance(value, uuid.UUID):
            return uuid.UUID(str(value))
        return value


class Base(DeclarativeBase):
    pass


class User(Base):
    """Parent account"""
    __tablename__ = "users"

    id = Column(GUID(), primary_key=True, default=uuid.uuid4)
    email = Column(String(255), unique=True, nullable=False, index=True)
    password_hash = Column(String(255), nullable=False)
    name = Column(String(100), nullable=False)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())

    # Relationships
    children = relationship("Child", back_populates="parent", cascade="all, delete-orphan")


class Child(Base):
    """Child profile under a parent account"""
    __tablename__ = "children"

    id = Column(GUID(), primary_key=True, default=uuid.uuid4)
    parent_id = Column(GUID(), ForeignKey("users.id"), nullable=False)
    name = Column(String(100), nullable=False)
    age = Column(Integer, nullable=False)
    avatar = Column(String(50), default="compass")  # vector badge key
    pin = Column(String(4), nullable=False)  # 4-digit PIN for child login
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    # Relationships
    parent = relationship("User", back_populates="children")
    playlists = relationship("Playlist", back_populates="child", cascade="all, delete-orphan")
    watch_progress = relationship("WatchProgress", back_populates="child", cascade="all, delete-orphan")


class Video(Base):
    """Cached YouTube video metadata + AI scores"""
    __tablename__ = "videos"

    id = Column(GUID(), primary_key=True, default=uuid.uuid4)
    youtube_id = Column(String(20), unique=True, nullable=False, index=True)
    title = Column(String(500), nullable=False)
    channel_name = Column(String(200))
    channel_id = Column(String(50))
    duration_seconds = Column(Integer)
    thumbnail_url = Column(String(500))
    description = Column(Text)
    transcript = Column(Text)

    # Subject classification
    subject = Column(String(100))
    subject_tags = Column(Text)  # JSON array stored as text

    # Age targeting
    age_min = Column(Integer)
    age_max = Column(Integer)

    # AI scores (0-100)
    educational_score = Column(Integer)
    safety_score = Column(Integer)
    clarity_score = Column(Integer)
    overall_score = Column(Integer)

    # AI analysis
    ai_summary = Column(Text)
    red_flags = Column(Text)  # JSON array stored as text
    is_safe = Column(Boolean, default=True)
    quiz_json = Column(Text)  # JSON object storing the 3-question quiz

    # Metadata
    published_at = Column(DateTime(timezone=True))
    view_count = Column(Integer)
    cached_at = Column(DateTime(timezone=True), server_default=func.now())
    ai_analyzed_at = Column(DateTime(timezone=True))

    # Relationships
    playlist_items = relationship("PlaylistItem", back_populates="video")
    watch_progress = relationship("WatchProgress", back_populates="video")


class Playlist(Base):
    """A parent-curated playlist assigned to a child for a specific date"""
    __tablename__ = "playlists"

    id = Column(GUID(), primary_key=True, default=uuid.uuid4)
    child_id = Column(GUID(), ForeignKey("children.id"), nullable=False)
    title = Column(String(200), nullable=False)
    description = Column(Text)
    scheduled_date = Column(Date, nullable=True)  # None = template, not scheduled
    is_active = Column(Boolean, default=True)
    is_template = Column(Boolean, default=False)  # Reusable template
    total_duration_seconds = Column(Integer, default=0)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())

    # Relationships
    child = relationship("Child", back_populates="playlists")
    items = relationship("PlaylistItem", back_populates="playlist",
                         order_by="PlaylistItem.position", cascade="all, delete-orphan")


class PlaylistItem(Base):
    """A video within a playlist, with ordering"""
    __tablename__ = "playlist_items"

    id = Column(GUID(), primary_key=True, default=uuid.uuid4)
    playlist_id = Column(GUID(), ForeignKey("playlists.id"), nullable=False)
    video_id = Column(GUID(), ForeignKey("videos.id"), nullable=False)
    position = Column(Integer, nullable=False)  # 1-based ordering
    notes = Column(Text)  # Parent notes for this item

    # Relationships
    playlist = relationship("Playlist", back_populates="items")
    video = relationship("Video", back_populates="playlist_items")
    watch_progress = relationship("WatchProgress", back_populates="playlist_item")


class WatchProgress(Base):
    """Tracks what each child has watched"""
    __tablename__ = "watch_progress"

    id = Column(GUID(), primary_key=True, default=uuid.uuid4)
    child_id = Column(GUID(), ForeignKey("children.id"), nullable=False)
    video_id = Column(GUID(), ForeignKey("videos.id"), nullable=False)
    playlist_item_id = Column(GUID(), ForeignKey("playlist_items.id"), nullable=True)
    watched_seconds = Column(Integer, default=0)
    completed = Column(Boolean, default=False)
    watched_at = Column(DateTime(timezone=True), server_default=func.now())
    completed_at = Column(DateTime(timezone=True))

    # Relationships
    child = relationship("Child", back_populates="watch_progress")
    video = relationship("Video", back_populates="watch_progress")
    playlist_item = relationship("PlaylistItem", back_populates="watch_progress")
