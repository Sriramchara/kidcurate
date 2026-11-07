from pydantic_settings import BaseSettings, SettingsConfigDict
from typing import List
from pathlib import Path
import json

ENV_PATH = Path(__file__).resolve().parent.parent / ".env"


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=str(ENV_PATH),
        extra="ignore",
    )

    # App
    APP_NAME: str = "KidCurate"
    DEBUG: bool = False
    CORS_ORIGINS: str = '["http://localhost:3000"]'

    # Database
    DATABASE_URL: str = "postgresql+asyncpg://kidcurate:password@localhost:5432/kidcurate_db"

    # JWT
    SECRET_KEY: str = "change-this-in-production-use-long-random-string"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 10080  # 7 days

    # External APIs
    YOUTUBE_API_KEY: str = ""
    GEMINI_API_KEY: str = ""

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
