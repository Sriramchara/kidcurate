@echo off
set ADB="%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"

if not exist %ADB% (
    set ADB=adb
)

echo [1/3] Reverse-port forwarding port 8000 for backend access...
%ADB% reverse tcp:8000 tcp:8000

echo [2/3] Installing KidCurate to your connected Android phone...
%ADB% push "%~dp0android\app\build\outputs\apk\debug\app-debug.apk" /data/local/tmp/app.apk
%ADB% shell pm install -r /data/local/tmp/app.apk

echo [3/3] Launching KidCurate...
%ADB% shell am start -n com.kidcurate.app/.MainActivity

echo.
echo KidCurate is installed and running on your phone!

