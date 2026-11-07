# KidCurate Backend

FastAPI backend for the KidCurate parent-controlled YouTube learning platform.

## Quick Start

### 1. Install Python Dependencies

```bash
pip install uv
uv pip install -e .
```

Or with regular pip:
```bash
pip install fastapi uvicorn sqlalchemy asyncpg psycopg2-binary python-jose passlib \
  python-multipart httpx google-generativeai youtube-transcript-api pydantic \
  pydantic-settings python-dotenv email-validator
```

### 2. Set Up PostgreSQL

```bash
# Create database
createdb kidcurate_db
createuser kidcurate --password
# Grant access
psql -c "GRANT ALL PRIVILEGES ON DATABASE kidcurate_db TO kidcurate;"
```

### 3. Configure Environment

```bash
cp .env.example .env
# Edit .env and fill in:
# - DATABASE_URL
# - SECRET_KEY (generate: python -c "import secrets; print(secrets.token_hex(32))")
# - YOUTUBE_API_KEY
# - GEMINI_API_KEY
```

### 4. Run the Server

```bash
uvicorn app.main:app --reload --port 8000
```

API docs available at: http://localhost:8000/docs

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Parent registration |
| POST | /api/auth/login | Parent login |
| GET | /api/auth/me | Get current user |
| GET | /api/children/ | List children |
| POST | /api/children/ | Create child profile |
| POST | /api/children/verify-pin | Child PIN verify (Android) |
| GET | /api/videos/search | Search YouTube videos |
| POST | /api/videos/cache/{id} | Cache a video |
| GET | /api/playlists/ | List playlists |
| POST | /api/playlists/ | Create playlist |
| POST | /api/playlists/{id}/items | Add video to playlist |
| PUT | /api/playlists/{id}/reorder | Reorder playlist |
| GET | /api/playlists/child/{id}/today | Today's playlist (Android) |
| POST | /api/progress/update | Update watch progress (Android) |
| GET | /api/progress/child/{id}/summary | Progress summary |
| POST | /api/ai/curriculum | Generate AI curriculum |

## Project Structure

```
backend/
├── app/
│   ├── main.py          # FastAPI app + routes registration
│   ├── config.py        # Settings from environment
│   ├── database.py      # Async DB engine + session
│   ├── models.py        # SQLAlchemy ORM models
│   ├── auth.py          # JWT auth utilities
│   ├── routes/
│   │   ├── auth.py      # /api/auth/*
│   │   ├── children.py  # /api/children/*
│   │   ├── videos.py    # /api/videos/*
│   │   ├── playlists.py # /api/playlists/*
│   │   ├── progress.py  # /api/progress/*
│   │   └── ai.py        # /api/ai/*
│   └── services/
│       └── ai_scorer.py # Gemini video scoring
├── pyproject.toml
└── .env.example
```
