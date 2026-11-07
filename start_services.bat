@echo off
echo ===================================================
echo           Starting KidCurate Services
echo ===================================================

echo [1/3] Reverse-port forwarding port 8000 for Android ADB...
set ADB="%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
if not exist %ADB% set ADB=adb
%ADB% reverse tcp:8000 tcp:8000 >nul 2>&1

echo [2/3] Starting FastAPI Backend on port 8000...
start "KidCurate Backend (FastAPI)" cmd /k "cd /d "%~dp0backend" && .venv\Scripts\python.exe -m uvicorn app.main:app --host 0.0.0.0 --port 8000"

echo [3/3] Starting Next.js Web Portal on port 3000...
start "KidCurate Web Portal (Next.js)" cmd /k "cd /d "%~dp0web-portal" && npm run dev"

echo.
echo All services launched!
echo - FastAPI Backend: http://localhost:8000 (or PC Wi-Fi IP:8000)
echo - Web Portal:      http://localhost:3000
echo - Android App:     Connected via ADB / Wi-Fi
echo ===================================================
timeout /t 3 >nul
start http://localhost:3000/dashboard

