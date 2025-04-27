@echo off
echo Movie Streaming App Build Script
echo ------------------------------

if not exist bin mkdir bin

echo.
echo Compiling Java files...
javac -cp "lib/*" -d bin src/*.java

if %errorlevel% neq 0 (
    echo.
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)

echo.
echo Compilation successful!
echo.

echo Choose an option:
echo 1. Run the main application
echo 2. Initialize database with test data
echo.

set /p choice="Enter your choice (1 or 2): "

if "%choice%"=="1" (
    echo.
    echo Running the main application...
    echo.
    java -cp "bin;lib/*" MovieStreamingApp
) else if "%choice%"=="2" (
    echo.
    echo Initializing database with test data...
    echo.
    java -cp "bin;lib/*" DatabaseTest
) else (
    echo.
    echo Invalid choice!
)

pause 