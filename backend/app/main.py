import logging
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse, HTMLResponse
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
from app.config import settings
from app.database import create_tables
from app.routes import auth, children, videos, playlists, progress

logger = logging.getLogger("uvicorn.error")


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup: create DB tables
    try:
        await create_tables()
        logger.info("Database initialized successfully.")
        from app.database import AsyncSessionLocal
        from app.routes.children import remediate_plaintext_pins
        async with AsyncSessionLocal() as session:
            count = await remediate_plaintext_pins(session)
            if count > 0:
                logger.info(f"Remediated {count} legacy child PINs to secure bcrypt hashes.")
    except Exception as e:
        logger.error(f"Database initialization deferred: {e}")
    yield
    # Shutdown: nothing special needed


app = FastAPI(
    title="KidCurate API",
    description="Parent-controlled YouTube learning platform backend",
    version="1.0.0",
    lifespan=lifespan,
)

# CORS — allow Next.js portal, configured domains, and mobile clients
origin_regex = r"^(https?://(localhost|127\.0\.0\.1|192\.168\.\d+\.\d+|10\.\d+\.\d+\.\d+)(:\d+)?|https://([a-zA-Z0-9-]+\.)*(vercel\.app|onrender\.com))$"

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins_list,
    allow_origin_regex=origin_regex,
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE", "OPTIONS"],
    allow_headers=["Authorization", "Content-Type", "Accept"],
)

# Security Headers Middleware
@app.middleware("http")
async def add_security_headers(request: Request, call_next):
    response = await call_next(request)
    response.headers["X-Content-Type-Options"] = "nosniff"
    response.headers["X-Frame-Options"] = "SAMEORIGIN"
    response.headers["X-XSS-Protection"] = "1; mode=block"
    response.headers["Strict-Transport-Security"] = "max-age=31536000; includeSubDomains"
    response.headers["Referrer-Policy"] = "strict-origin-when-cross-origin"
    response.headers["Content-Security-Policy"] = (
        "default-src 'self'; "
        "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; "
        "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://fonts.googleapis.com; "
        "font-src 'self' https://fonts.gstatic.com; "
        "img-src 'self' data: https://*.ytimg.com https://*.youtube.com; "
        "frame-src https://www.youtube.com https://www.youtube-nocookie.com;"
    )
    return response

# Include all routers
app.include_router(auth.router)
app.include_router(children.router)
app.include_router(videos.router)
app.include_router(playlists.router)
app.include_router(progress.router)


@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    logger.exception(f"Unhandled server error on {request.method} {request.url.path}: {exc}")
    return JSONResponse(
        status_code=500,
        content={"detail": "An internal server error occurred. Please try again later."},
    )


@app.get("/")
async def root():
    return {
        "app": "KidCurate API",
        "version": "1.0.0",
        "status": "running",
        "docs": "/docs",
    }


@app.get("/health")
@app.get("/health/live")
@app.get("/health/ready")
async def health():
    return {"status": "ok"}


@app.get("/privacy", response_class=HTMLResponse)
async def privacy_policy():
    return """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>KidCurate - Privacy Policy</title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; line-height: 1.6; max-width: 800px; margin: 0 auto; padding: 24px; color: #1e293b; background: #f8fafc; }
        .card { background: #ffffff; padding: 32px; border-radius: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05), 0 2px 4px -2px rgba(0,0,0,0.05); }
        h1 { color: #4338ca; border-bottom: 2px solid #e2e8f0; padding-bottom: 12px; }
        h2 { color: #3730a3; margin-top: 24px; }
        ul { padding-left: 20px; }
        li { margin-bottom: 8px; }
        .highlight { background: #eef2ff; border-left: 4px solid #6366f1; padding: 12px 16px; border-radius: 4px; margin: 16px 0; }
        a { color: #4f46e5; text-decoration: none; font-weight: 600; }
        a:hover { text-decoration: underline; }
    </style>
</head>
<body>
    <div class="card">
        <h1>KidCurate Privacy Policy & Legal Disclosures</h1>
        <p><strong>Last Updated:</strong> November 16, 2026</p>
        <p>KidCurate is an educational, parent-controlled video curation platform providing children with safe access to wholesome YouTube videos without comments, external links, or recommendation algorithms.</p>
        
        <div class="highlight">
            <strong>YouTube API Services & Google Privacy Notice:</strong><br>
            KidCurate uses <strong>YouTube API Services</strong> to search and embed educational videos. By using KidCurate, you agree to be bound by the 
            <a href="https://www.youtube.com/t/terms" target="_blank" rel="noopener">YouTube Terms of Service</a> and acknowledge that Google processes information in accordance with the 
            <a href="https://policies.google.com/privacy" target="_blank" rel="noopener">Google Privacy Policy</a>.
        </div>

        <h2>1. Children's Privacy (COPPA & GDPR-K Compliance)</h2>
        <p>KidCurate is strictly a parent-managed tool. We do not knowingly collect personal information directly from children under 13.</p>
        <ul>
            <li><strong>Parent Information:</strong> Parent email and encrypted password for account synchronization.</li>
            <li><strong>Child Profile Information:</strong> Child's first name/nickname, age, avatar icon, and a 4-digit PIN set by the parent.</li>
            <li><strong>Zero Tracking:</strong> We never collect location data, photos, voice recordings, contacts, or advertising identifiers from children.</li>
        </ul>

        <h2>2. YouTube Data & Video Streaming Rules</h2>
        <ul>
            <li><strong>No Video Downloads:</strong> Videos are streamed live using YouTube's official embed player. KidCurate never downloads or stores raw video/audio files.</li>
            <li><strong>Creator Attribution:</strong> Channel names are displayed alongside all video titles.</li>
            <li><strong>No Background Play:</strong> Video playback stops automatically whenever the player screen is closed.</li>
        </ul>

        <h2>3. Data Deletion & Parental Rights</h2>
        <p>Parents can delete any child profile or permanently delete their entire parent account and all cloud data directly from <em>Parent Zone &rarr; Settings &rarr; Delete Account</em>.</p>

        <h2>4. Contact</h2>
        <p>For questions or data privacy inquiries, contact us at <a href="mailto:support@kidcurate.app">support@kidcurate.app</a>.</p>
    </div>
</body>
</html>"""


@app.get("/terms", response_class=HTMLResponse)
async def terms_of_service():
    return """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>KidCurate - Terms of Service</title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; line-height: 1.6; max-width: 800px; margin: 0 auto; padding: 24px; color: #1e293b; background: #f8fafc; }
        .card { background: #ffffff; padding: 32px; border-radius: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05), 0 2px 4px -2px rgba(0,0,0,0.05); }
        h1 { color: #4338ca; border-bottom: 2px solid #e2e8f0; padding-bottom: 12px; }
        h2 { color: #3730a3; margin-top: 24px; }
        ul { padding-left: 20px; }
        li { margin-bottom: 8px; }
        a { color: #4f46e5; text-decoration: none; font-weight: 600; }
        a:hover { text-decoration: underline; }
    </style>
</head>
<body>
    <div class="card">
        <h1>KidCurate Terms of Service</h1>
        <p><strong>Last Updated:</strong> November 16, 2026</p>
        
        <h2>1. The Service</h2>
        <p>KidCurate is a parent-operated mobile and web application allowing parents to curate custom, safe playlists of YouTube videos for their children.</p>

        <h2>2. Third-Party Content & YouTube API</h2>
        <p>KidCurate uses the <strong>YouTube Data API v3</strong> and YouTube's official embed player. By using KidCurate, you agree to be bound by the <a href="https://www.youtube.com/t/terms" target="_blank" rel="noopener">YouTube Terms of Service</a> and Google Privacy Policy. KidCurate does not host, store, or own any video content.</p>

        <h2>3. Parental Responsibility</h2>
        <p>Parents are solely responsible for reviewing and approving videos added to playlists and supervising their children's access.</p>

        <h2>4. Account Deletion</h2>
        <p>Parents may delete their account and all associated child profiles at any time via the in-app settings.</p>

        <h2>5. Contact</h2>
        <p>Questions regarding these terms may be directed to <a href="mailto:legal@kidcurate.app">legal@kidcurate.app</a>.</p>
    </div>
</body>
</html>"""
