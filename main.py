"""
Root wrapper for KidCurate Backend deployment on Render / PaaS platforms.
Ensures Python locates the `backend/` directory regardless of whether the
working directory is repository root or `backend/`.
"""
import sys
import os

backend_path = os.path.join(os.path.dirname(__file__), "backend")
if backend_path not in sys.path:
    sys.path.insert(0, backend_path)

from app.main import app  # noqa: F401

