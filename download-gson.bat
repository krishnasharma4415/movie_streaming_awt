@echo off
echo Downloading Gson Library
echo -------------------------

set GSON_VERSION=2.10.1
set DOWNLOAD_URL=https://repo1.maven.org/maven2/com/google/code/gson/gson/%GSON_VERSION%/gson-%GSON_VERSION%.jar
set OUTPUT_FILE=lib/gson-%GSON_VERSION%.jar

echo Downloading Gson %GSON_VERSION% from Maven Central...
curl -L %DOWNLOAD_URL% -o %OUTPUT_FILE%

if %errorlevel% neq 0 (
    echo Failed to download Gson library!
    exit /b %errorlevel%
)

echo.
echo Gson library downloaded successfully to %OUTPUT_FILE%
echo.

pause 