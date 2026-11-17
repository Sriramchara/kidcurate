from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import model_validator
from typing import List
from pathlib import Path
import json
import os

ENV_PATH = Path(__file__).resolve().parent.parent / ".env"


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=str(ENV_PATH),
        extra="ignore",
    )

    # App
    APP_NAME: str = "KidCurate"
    DEBUG: bool = False
    ENVIRONMENT: str = "development"
    CORS_ORIGINS: str = '["http://localhost:3000"]'

    # Database
    DATABASE_URL: str = "postgresql+asyncpg://kidcurate:password@localhost:5432/kidcurate_db"

    # JWT
    SECRET_KEY: str = "change-this-in-production-use-long-random-string"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 15  # Short-lived access token: 15 minutes
    REFRESH_TOKEN_EXPIRE_MINUTES: int = 10080  # Refresh token: 7 days

    # External APIs & Authentication
    GOOGLE_CLIENT_ID: str = "179684236627-d7j0s15cvkj6li430jku6easqma5q81l.apps.googleusercontent.com"
    YOUTUBE_API_KEY: str = ""

    # Email SMTP Settings (for password resets and notifications)
    SMTP_HOST: str = ""
    SMTP_PORT: int = 587
    SMTP_USER: str = ""
    SMTP_PASSWORD: str = ""
    SMTP_FROM: str = "noreply@kidcurate.com"

    @model_validator(mode="after")
    def validate_production_secrets(self):
        is_prod = self.ENVIRONMENT.lower() in ("production", "prod") or bool(os.environ.get("RENDER"))
        if is_prod:
            if "change-this-in-production" in self.SECRET_KEY or len(self.SECRET_KEY) < 32:
                raise ValueError("FATAL: In production, SECRET_KEY must be a cryptographically secure random string of at least 32 characters.")
            if self.DATABASE_URL.strip().lower().startswith("sqlite"):
                raise ValueError("FATAL: Production must use persistent PostgreSQL storage, not local SQLite.")
            if not self.GOOGLE_CLIENT_ID.endswith(".apps.googleusercontent.com"):
                raise ValueError("FATAL: GOOGLE_CLIENT_ID must be a valid Google OAuth web client ID.")
        return self

    @property
    def cors_origins_list(self) -> List[str]:
        try:
            val = json.loads(self.CORS_ORIGINS)
            if isinstance(val, list):
                return val
            return [str(val)]
        except Exception:
            origins = [o.strip() for o in self.CORS_ORIGINS.split(",") if o.strip()]
            return origins if origins else ["http://localhost:3000"]


settings = Settings()
