@echo off
echo Building Android APK...
call gradlew.bat assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Build successful!
    echo APK location: app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo To install on connected device:
    echo adb install app\build\outputs\apk\debug\app-debug.apk
) else (
    echo.
    echo Build failed. Please check the errors above.
)
pause
