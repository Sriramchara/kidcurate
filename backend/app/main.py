import logging
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
from app.config import settings
from app.database import create_tables
from app.routes import auth, children, videos, playlists, progress, ai

logger = logging.getLogger("uvicorn.error")


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup: create DB tables
    await create_tables()
    yield
    # Shutdown: nothing special needed


app = FastAPI(
    title="KidCurate API",
    description="Parent-controlled YouTube learning platform backend",
    version="1.0.0",
    lifespan=lifespan,
)

# CORS — allow Next.js portal, Netlify, custom domain, and Android
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins_list,
    allow_origin_regex=r"https?://(localhost|127\.0\.0\.1|192\.168\.\d+\.\d+|10\.\d+\.\d+\.\d+|.*\.netlify\.app|.*testeenspace\.site)(:\d+)?",
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include all routers
app.include_router(auth.router)
app.include_router(children.router)
app.include_router(videos.router)
app.include_router(playlists.router)
app.include_router(progress.router)
app.include_router(ai.router)


@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    logger.exception(f"Unhandled server error on {request.method} {request.url.path}: {exc}")
    return JSONResponse(
        status_code=500,
        content={"detail": "An internal server error occurred", "message": str(exc)},
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
