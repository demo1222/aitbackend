@echo off
SETLOCAL EnableDelayedExpansion

ECHO ===== AIT Backend Deployment to Google Cloud Run =====
ECHO.

SET PROJECT_ID=vivid-gantry-458115-v3
SET REGION=europe-north2
SET SERVICE_NAME=aitbackend
SET REGISTRY=%REGION%-docker.pkg.dev/%PROJECT_ID%/aitbackend
SET IMAGE_NAME=%REGISTRY%/%SERVICE_NAME%
SET IMAGE_TAG=%IMAGE_NAME%:latest

REM Check if .env file exists and load environment variables
SET "JWT_SECRET=YOUR_JWT_SECRET_GOES_HERE"
SET "DB_PASSWORD=aitbackend"
SET "DB_USERNAME=aitbackend"
SET "DB_URL=jdbc:postgresql://34.51.179.11:5432/postgres"

IF EXIST .env (
    FOR /F "tokens=1,2 delims==" %%a IN (.env) DO (
        IF "%%a"=="JWT_SECRET" SET "JWT_SECRET=%%b"
        IF "%%a"=="DB_PASSWORD" SET "DB_PASSWORD=%%b"
        IF "%%a"=="DB_USERNAME" SET "DB_USERNAME=%%b"
        IF "%%a"=="DB_URL" SET "DB_URL=%%b"
    )
)

ECHO.
ECHO Step 1: Building Docker image...
ECHO.
docker build -t %IMAGE_TAG% .
IF %ERRORLEVEL% NEQ 0 (
    ECHO Error: Docker build failed!
    EXIT /B %ERRORLEVEL%
)

REM Uncomment these lines if you haven't configured Docker authentication with GCP yet
@REM ECHO.
@REM ECHO Step 2: Configure Docker to authenticate with GCP Artifact Registry...
@REM ECHO.
@REM gcloud auth configure-docker %REGION%-docker.pkg.dev
@REM IF %ERRORLEVEL% NEQ 0 (
@REM     ECHO Error: Docker authentication with GCP failed!
@REM     EXIT /B %ERRORLEVEL%
@REM )

ECHO.
ECHO Step 3: Pushing image to GCP Artifact Registry...
ECHO.
docker push %IMAGE_TAG%
IF %ERRORLEVEL% NEQ 0 (
    ECHO Error: Docker push failed!
    EXIT /B %ERRORLEVEL%
)

ECHO.
ECHO Step 4: Deploying to Cloud Run...
ECHO.
gcloud run deploy %SERVICE_NAME% ^
  --image=%IMAGE_TAG% ^
  --platform=managed ^
  --region=%REGION% ^
  --allow-unauthenticated ^
  --memory=1Gi ^
  --min-instances=0 ^
  --max-instances=10 ^
  --cpu=1 ^
  --port=8080 ^
  --timeout=300 ^
  --concurrency=80 ^
  --set-env-vars="SPRING_DATASOURCE_URL=%DB_URL%,SPRING_DATASOURCE_USERNAME=%DB_USERNAME%,SPRING_DATASOURCE_PASSWORD=%DB_PASSWORD%,JWT_SECRET=%JWT_SECRET%,SPRING_PROFILES_ACTIVE=prod"
IF %ERRORLEVEL% NEQ 0 (
    ECHO Error: Deployment to Cloud Run failed!
    EXIT /B %ERRORLEVEL%
)

ECHO.
ECHO ===== Deployment Complete =====
ECHO.

REM Get and display the deployed service URL
ECHO Fetching application URL...
FOR /F "tokens=*" %%i IN ('gcloud run services describe %SERVICE_NAME% --platform=managed --region=%REGION% --format="value(status.url)"') DO (
    SET SERVICE_URL=%%i
)
ECHO.
ECHO Your AIT Backend is deployed and available at: !SERVICE_URL!
ECHO.
ECHO API Endpoints:
ECHO - Health Check: !SERVICE_URL!/actuator/health
ECHO - Auth: !SERVICE_URL!/api/auth/
ECHO - WebSocket: !SERVICE_URL!/ws
ECHO.

REM Ask if user wants to clean up old images
SET /P CLEANUP="Do you want to clean up old/unused Docker images? (y/n, default=n): "
IF /I "%CLEANUP%"=="y" (
    ECHO.
    ECHO Cleaning up old Docker images...
    docker image prune -f
    ECHO Cleanup complete.
)

REM Ask if user wants to view logs
SET /P VIEW_LOGS="Do you want to view the deployment logs? (y/n, default=n): "
IF /I "%VIEW_LOGS%"=="y" (
    ECHO.
    ECHO Fetching recent logs...
    gcloud run logs read %SERVICE_NAME% --region=%REGION% --limit=50
)

ENDLOCAL
