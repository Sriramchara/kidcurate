# KidCurate: Complete System Dossier & ChatGPT Master Context

> **How to use this file with ChatGPT:**  
> Copy and paste this document into any ChatGPT or Claude conversation to give the AI full architectural, database, API, and codebase context. It includes a ready-to-use **System Prompt Directive**, architecture diagrams, exact file maps, and prompt recipes for adding new features.

---

## 🤖 ChatGPT Master System Prompt

*Copy and paste the block below into your ChatGPT custom instructions or chat session:*

```text
You are the Lead Staff Engineer on "KidCurate", an ad-free, parent-controlled YouTube educational platform for kids.

KidCurate consists of three tiers:
1. Backend: Python 3.11+, FastAPI, SQLAlchemy 2.0 (Async), PostgreSQL/SQLite with custom cross-platform GUID type, Google Gemini 1.5 Flash (google-generativeai), YouTube Data API v3, youtube-transcript-api.
2. Parent Web Portal: Next.js 16 (App Router), React 19, TypeScript, Tailwind CSS v4, Lucide Icons, Axios.
3. Child Android App: Native Kotlin, Jetpack Compose, Material 3, Retrofit 2 + OkHttp, PierfrancescoSoffritti/android-youtube-player.

Engineering Rules you must ALWAYS follow when generating code:
- Async SQLAlchemy 2.0 only: Use select(...), db.execute(...), and scalars().all(). Never use legacy db.query() or synchronous engine calls.
- Cross-DB GUID Type: All primary and foreign keys use the GUID() TypeDecorator to support both PostgreSQL native UUID and SQLite CHAR(36) transparently without hex conversion crashes.
- Next.js 16 App Router: Any component utilizing React hooks (useState, useEffect, useParams, useRouter) must declare 'use client'; at the very top.
- Distraction-Free Child Security: Under NO circumstance should YouTube recommendations, external links, comments, or autoplay ads be accessible to the child on the Android client. Playback must be locked to parent-curated playlists.
- AI Graceful Degradation: If Gemini or YouTube transcript API fails or is rate-limited, fallback quizzes and search results must always ensure a child or parent is never locked out or faced with unhandled 500 exceptions.
```

---

## 1. Project Mission & Core Concepts

Traditional YouTube is unsuited for independent child learning due to commercial advertising, clickbait thumbnails, algorithm rabbit holes, and distracting comment sections. 

**KidCurate** solves this through a parent-in-the-loop, AI-guided learning architecture:
* **The Parent Curies:** Parents search YouTube through safe, embeddable-only filters, inspect AI educational/safety ratings (0–100), generate multi-day AI curriculums, organize playlists with drag-and-drop, and track watch progress.
* **The Child Learns:** Kids log in using their personal 4-digit PIN on a tablet or phone, view only parent-approved playlists for the day, watch videos in a distraction-free player, and take a quick 3-question AI comprehension quiz before earning their completion reward.

---

## 2. Tech Stack Matrix

| Tier | Component | Technology / Stack | Key Highlights |
| :--- | :--- | :--- | :--- |
| **Backend** | API Server | Python 3.11+, FastAPI | Asynchronous routes, Pydantic v2 schemas, JWT Bearer tokens |
| **Backend** | Database & ORM | PostgreSQL / SQLite | SQLAlchemy 2.0 Async, `asyncpg`, `aiosqlite`, cross-platform `GUID` |
| **Backend** | AI & Video | Google Gemini 1.5 Flash | `google-generativeai`, YouTube Data API v3, `youtube-transcript-api` |
| **Frontend** | Parent Portal | Next.js 16, React 19 | Tailwind CSS v4, Radix UI, Lucide Icons, Axios, embedded video modal |
| **Mobile** | Child App | Kotlin, Jetpack Compose | Material 3, Retrofit 2 + Gson, Coroutines StateFlow, YouTube IFrame Player |

---

## 3. End-to-End Sequence Diagrams

### A. Child App Daily Learning Workflow
```mermaid
sequenceDiagram
    autonumber
    actor Kid as Child (Android App)
    participant Android as Jetpack Compose Client
    participant API as FastAPI Backend
    participant DB as Database (Postgres/SQLite)
    participant Gemini as Google Gemini 1.5

    Kid->>Android: Opens app & selects profile (e.g. Aarav)
    Android->>API: GET /api/children/profiles
    API->>DB: Query child public records
    DB-->>API: Return [id, name, avatar]
    API-->>Android: Display friendly profile cards

    Kid->>Android: Enters 4-digit PIN
    Android->>API: POST /api/children/verify-pin {child_id, pin}
    API->>DB: Compare PIN
    DB-->>API: Valid PIN
    API-->>Android: {verified: true}

    Android->>API: GET /api/playlists/child/{id}/today
    API->>DB: Fetch today's scheduled or active playlist
    DB-->>API: Playlist + ordered videos
    API-->>Android: Display playlist queue

    Kid->>Android: Starts Video 1
    Android->>API: POST /api/progress/update {watched_seconds, completed: false}
    
    Kid->>Android: Video Finishes
    Android->>API: GET /api/ai/quiz/{youtube_id}
    alt Quiz Cached
        API->>DB: Read quiz_json
        DB-->>API: 3 Questions
    else Quiz Not Cached
        API->>API: Fetch transcript via youtube-transcript-api
        API->>Gemini: Generate 3-question comprehension quiz
        Gemini-->>API: Questions JSON
        API->>DB: Cache quiz_json on Video
    end
    API-->>Android: Quiz JSON
    
    Android->>Kid: Display interactive QuizScreen
    Kid->>Android: Answers all 3 questions
    Android->>API: POST /api/progress/update {completed: true}
    Android->>Kid: Celebratory Completion Screen!
```

### B. Parent Video Discovery & AI Curriculum Flow
```mermaid
sequenceDiagram
    autonumber
    actor Parent as Parent (Web Portal)
    participant Web as Next.js 16 Web Portal
    participant API as FastAPI Backend
    participant YT as YouTube Data API v3
    participant Gemini as Google Gemini 1.5

    Parent->>Web: Searches "Solar System for Grade 2" + AI Score toggle
    Web->>API: GET /api/videos/search?q=...&ai_score=true
    API->>YT: search.list(safeSearch=strict, videoEmbeddable=true)
    YT-->>API: Video candidates
    API->>YT: videos.list(duration, licensing, statistics)
    YT-->>API: Filtered duration (>60s, <60m)
    API->>Gemini: Evaluate educational value, clarity, and safety flags
    Gemini-->>API: {educational: 92, safety: 98, overall: 94, tags, summary}
    API-->>Web: Video search cards with score badges
    
    Parent->>Web: Clicks "Preview"
    Web->>Web: Opens in-modal distraction-free YouTube iframe
    Parent->>Web: Clicks "+ Add to Playlist"
    Web->>API: POST /api/playlists/{id}/items
    API-->>Web: Playlist updated with next position index
```

---

## 4. Repository Directory Structure

```
kidcurate/
├── PROJECT_OVERVIEW.md       # Master project documentation for LLMs & developers
├── backend/                  # FastAPI Application & AI Pipeline
│   ├── app/
│   │   ├── routes/
│   │   │   ├── auth.py       # Parent signup (/register), login (/login), current user (/me)
│   │   │   ├── children.py   # Child CRUD, /profiles (public list), /verify-pin
│   │   │   ├── videos.py     # YouTube search with strict filtering, caching & scoring
│   │   │   ├── playlists.py  # Playlist CRUD, drag-and-drop reordering, /child/{id}/today
│   │   │   ├── progress.py   # Progress update, watch history & analytics summary
│   │   │   └── ai.py         # /api/ai/curriculum (roadmap generator), /api/ai/quiz/{id}
│   │   ├── services/
│   │   │   └── ai_scorer.py  # Gemini prompts, transcript extraction & fallback quiz generator
│   │   ├── auth.py           # Password hashing (bcrypt) & JWT decoding dependencies
│   │   ├── config.py         # Settings loaded from backend/.env via Pydantic
│   │   ├── database.py       # Async engine & sessionmaker, startup create_tables()
│   │   ├── models.py         # SQLAlchemy Base models with custom GUID TypeDecorator
│   │   └── main.py           # FastAPI app instance, CORS middleware, lifespan events
│   ├── pyproject.toml        # Dependencies & package specifications
│   └── .env                  # Environment variables
│
├── web-portal/               # Next.js 16 + React 19 Parent Management Portal
│   ├── src/
│   │   ├── app/
│   │   │   ├── layout.tsx    # Root layout & global dark theme
│   │   │   ├── page.tsx      # Sign In / Create Account landing page
│   │   │   ├── dashboard/    # Parent dashboard: child cards, quick stats, add child modal
│   │   │   └── children/[id]/
│   │   │       ├── page.tsx  # Playlist manager, in-modal preview, AI Curriculum Designer
│   │   │       ├── search/   # Safe YouTube search, AI score pills, preview player
│   │   │       └── reports/  # Watch duration progress bars, completion rates, history log
│   │   └── lib/
│   │       └── api.ts        # Typed fetch client with automated JWT Bearer injection
│   ├── package.json
│   └── tailwind.config.ts
│
└── android/                  # Native Kotlin Jetpack Compose Child App
    ├── app/
    │   ├── src/main/
    │   │   ├── java/com/kidcurate/app/
    │   │   │   ├── MainActivity.kt        # Compose activity hosting AnimatedContent screen router
    │   │   │   ├── KidViewModel.kt        # StateFlow app state, profile loader & progress reporting
    │   │   │   ├── data/
    │   │   │   │   ├── ApiClient.kt       # Retrofit 2 + OkHttp client singleton
    │   │   │   │   └── Models.kt          # Kotlin DTOs (ChildProfileItem, VideoItem, etc.)
    │   │   │   └── ui/
    │   │   │       ├── screens/
    │   │   │       │   ├── ChildSelectScreen.kt      # Multi-child avatar selection grid
    │   │   │       │   ├── PinEntryScreen.kt         # PIN pad with custom keypad & profile switcher
    │   │   │       │   ├── TodaysPlaylistScreen.kt   # Daily queue of assigned lessons
    │   │   │       │   ├── PlayerScreen.kt           # Embedded YouTube player with auto-landscape
    │   │   │       │   ├── QuizScreen.kt             # 3-question AI comprehension quiz
    │   │   │       │   └── CompletionScreen.kt       # Reward celebration screen
    │   │   │       └── theme/                        # Colors, typography, shapes
    │   │   └── AndroidManifest.xml
    │   └── build.gradle.kts
    └── settings.gradle.kts
```

---

## 5. Database Schema & Models

PostgreSQL or SQLite are both supported out of the box using SQLAlchemy 2.0 Async.

```mermaid
erDiagram
    User ||--o{ Child : "has many"
    Child ||--o{ Playlist : "has many"
    Child ||--o{ WatchProgress : "records"
    Playlist ||--o{ PlaylistItem : "contains"
    Video ||--o{ PlaylistItem : "referenced by"
    Video ||--o{ WatchProgress : "tracked in"

    User {
        guid id PK
        string email UK
        string password_hash
        string name
        boolean is_active
        datetime created_at
    }

    Child {
        guid id PK
        guid parent_id FK
        string name
        int age
        string avatar "emoji"
        string pin "4 digits"
        datetime created_at
    }

    Video {
        guid id PK
        string youtube_id UK
        string title
        string channel_name
        int duration_seconds
        string thumbnail_url
        int educational_score "0-100"
        int safety_score "0-100"
        int clarity_score "0-100"
        int overall_score "0-100"
        text ai_summary
        json subject_tags
        json quiz_json
        boolean is_safe
    }

    Playlist {
        guid id PK
        guid child_id FK
        string title
        text description
        date scheduled_date
        boolean is_active
        boolean is_template
        int total_duration_seconds
    }

    PlaylistItem {
        guid id PK
        guid playlist_id FK
        guid video_id FK
        int position
        text notes
    }

    WatchProgress {
        guid id PK
        guid child_id FK
        guid video_id FK
        guid playlist_item_id FK
        int watched_seconds
        boolean completed
        datetime watched_at
        datetime completed_at
    }
```

### Cross-Platform `GUID` Type Implementation
To ensure zero friction when deploying across SQLite (development) and PostgreSQL (production), the models use a custom SQLAlchemy `TypeDecorator`:
```python
class GUID(TypeDecorator):
    impl = CHAR(36)
    cache_ok = True

    def load_dialect_impl(self, dialect):
        if dialect.name == "postgresql":
            return dialect.type_descriptor(PG_UUID(as_uuid=True))
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
```

---

## 6. Complete REST API Specifications

| Method | Endpoint | Auth Required | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | None | Create parent account (`name`, `email`, `password`) -> returns JWT |
| `POST` | `/api/auth/login` | None | Form-urlencoded login (`username`, `password`) -> returns JWT |
| `GET` | `/api/auth/me` | Bearer Token | Return currently logged-in parent details |
| `GET` | `/api/children/profiles` | **Public** | Public list of child profiles (`id`, `name`, `avatar`) for Android app selector |
| `GET` | `/api/children/` | Bearer Token | List all children belonging to logged-in parent |
| `POST` | `/api/children/` | Bearer Token | Create new child profile (`name`, `age`, `avatar`, `pin`) |
| `GET` | `/api/children/{id}` | Bearer Token | Fetch single child profile |
| `PUT` | `/api/children/{id}` | Bearer Token | Update child name, age, avatar, or 4-digit PIN |
| `DELETE`| `/api/children/{id}` | Bearer Token | Delete child profile and cascade-delete child playlists |
| `POST` | `/api/children/verify-pin` | **Public** | Verify child's 4-digit PIN before granting playback access |
| `GET` | `/api/videos/search` | Bearer Token | Safe YouTube search (`q`, `age`, `ai_score=true/false`) with AI rating |
| `POST` | `/api/videos/cache/{id}` | Bearer Token | Cache YouTube metadata in database |
| `GET` | `/api/playlists/` | Bearer Token | List playlists filtered by `child_id` and/or `scheduled_date` |
| `POST` | `/api/playlists/` | Bearer Token | Create daily playlist or reusable template |
| `GET` | `/api/playlists/{id}` | Bearer Token | Get playlist detail with ordered items |
| `PUT` | `/api/playlists/{id}` | Bearer Token | Update playlist title, description, or scheduled date |
| `DELETE`| `/api/playlists/{id}` | Bearer Token | Delete playlist |
| `POST` | `/api/playlists/{id}/items` | Bearer Token | Add video to playlist (assigns next sequence position) |
| `DELETE`| `/api/playlists/{id}/items/{itemId}` | Bearer Token | Remove video from playlist |
| `PUT` | `/api/playlists/{id}/reorder` | Bearer Token | Update full ordering with ordered list of `item_ids` |
| `GET` | `/api/playlists/child/{id}/today` | **Public** | Return today's scheduled active playlist for Android app |
| `POST` | `/api/progress/update` | **Public** | Record child watch time (`watched_seconds`) & completion status |
| `GET` | `/api/progress/child/{id}/summary` | Bearer Token | Aggregate analytics (total hours, completed count, top subjects) |
| `GET` | `/api/progress/child/{id}/history` | Bearer Token | Reverse-chronological watch activity log |
| `POST` | `/api/ai/curriculum` | Bearer Token | Generate multi-day AI learning roadmap (`child_age`, `subject`, `goals`, `num_days`) |
| `GET` | `/api/ai/quiz/{youtube_id}` | **Public** | Retrieve or generate 3-question comprehension quiz |

---

## 7. AI Pipeline & Safety Heuristics

### Video Scoring Formula
Videos evaluated with Gemini 1.5 Flash receive scores from 0 to 100:
$$\text{Overall Score} = (0.5 \times \text{Educational}) + (0.3 \times \text{Safety}) + (0.2 \times \text{Clarity})$$

1. **Educational Score:** Measures explanation pacing, active demonstration, curriculum alignment, and conceptual rigor.
2. **Safety Score:** Penalizes profanity, jump scares, commercial marketing/excessive toy ads, clickbait, and unverified claims.
3. **Clarity Score:** Audio cleanliness, age-level vocabulary, and visual clarity.

### Fallback Quiz Mechanism
If a YouTube video does not provide a caption transcript, the backend automatically generates comprehension questions from the video title, description, and tags, ensuring children are never blocked from completing their lessons.

---

## 8. Configuration & Environment Variables

### Backend (`backend/.env`)
```env
# Application
APP_NAME=KidCurate
DEBUG=true
CORS_ORIGINS=["http://localhost:3000","http://localhost:3001"]

# Database (SQLite default for zero-config local run; swap to Postgres for production)
DATABASE_URL=sqlite+aiosqlite:///C:/Users/srira/OneDrive/Desktop/kidcurate/backend/kidcurate.db
# DATABASE_URL=postgresql+asyncpg://kidcurate:password@localhost:5432/kidcurate_db

# JWT Authentication
SECRET_KEY=generate_with_python_secrets_token_hex_32
ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=10080

# External API Keys
YOUTUBE_API_KEY=AIzaSy...
GEMINI_API_KEY=AIzaSy...
```

### Parent Web Portal (`web-portal/.env.local`)
```env
NEXT_PUBLIC_API_URL=http://localhost:8000
```

### Android Application (`android/app/build.gradle.kts`)
```kotlin
defaultConfig {
    // Point to your workstation's local Wi-Fi IP (or http://10.0.2.2:8000 for standard emulator)
    buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.112:8000\"")
}
```

---

## 9. ChatGPT Prompt Playbook & Recipes

Below are pre-tested prompt templates you can copy directly into ChatGPT to generate features, tests, or bug fixes for KidCurate:

### Prompt 1: Add Daily Screen Time Limit Feature
> *"In the KidCurate codebase, I want to add a Daily Screen Time Limit for each child (e.g. 45 minutes max per day). Please provide: 1) SQLAlchemy model migration/update in `backend/app/models.py` to add `max_daily_minutes` to `Child`, 2) Updates in `backend/app/routes/progress.py` to calculate remaining time, 3) The UI field in the web portal 'Add/Edit Child' modal, and 4) A Compose lockout dialog in `PlayerScreen.kt` that triggers when today's limit is reached."*

### Prompt 2: Add Parent PIN Override in Android Player
> *"In the KidCurate Android app (`android/app/src/main/java/com/kidcurate/app`), create a Parent Settings dialog accessible from the top-right corner of `TodaysPlaylistScreen.kt`. It must prompt for the Parent's 4-digit Master PIN before allowing them to change the API server IP or manually mark videos as completed. Use Jetpack Compose Material 3 dialogs."*

### Prompt 3: Write Comprehensive FastAPI Pytest Suite
> *"Write a complete test suite for KidCurate backend using `pytest` and `httpx.AsyncClient`. Test: 1) Parent registration & login, 2) Child profile creation with valid and invalid PINs, 3) Public `/api/children/profiles` endpoint, 4) Playlist item creation and reordering, and 5) Progress recording. Use an in-memory SQLite database fixture."*

### Prompt 4: Production Dockerization
> *"Provide a complete `docker-compose.yml` for KidCurate that orchestrates: 1) PostgreSQL 16 database with automated volume backup, 2) FastAPI backend running with Gunicorn + Uvicorn workers, 3) Next.js web portal with multi-stage standalone build, and 4) Caddy reverse proxy with automatic SSL."*
