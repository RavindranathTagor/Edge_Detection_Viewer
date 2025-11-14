@echo off
echo ========================================
echo Cleaning Gradle Build Cache
echo ========================================
echo.

cd /d "%~dp0"

echo Cleaning local build directories...
if exist "app\build" rmdir /s /q "app\build"
if exist "build" rmdir /s /q "build"
if exist ".gradle" rmdir /s /q ".gradle"

echo.
echo Gradle cache cleaned successfully!
echo The new Gradle home is set to: D:\GradleHome
echo.
echo You can now:
echo 1. Sync your project in Android Studio
echo 2. Or run: gradlew clean build
echo.
pause

