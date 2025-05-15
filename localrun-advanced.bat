@echo off
echo ===== AIT Backend Docker Local Runner =====

REM Check if Docker is running
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo Docker is not running! Please start Docker Desktop and try again.
    exit /b 1
)

REM Check if a container with the same name already exists and remove it
echo Checking for existing containers...
docker ps -a | findstr "aitbackend-container" >nul
if %errorlevel% equ 0 (
    echo Found existing container, stopping and removing it...
    docker stop aitbackend-container >nul 2>&1
    docker rm aitbackend-container >nul 2>&1
)

REM Build the Docker image
echo Building Docker image for aitbackend...
docker build -t aitbackend:latest .
if %errorlevel% neq 0 (
    echo Failed to build Docker image!
    exit /b 1
)

REM Run the Docker container
echo Running Docker container with GCP SQL connection...
docker run -d -p 8080:8080 --name aitbackend-container aitbackend:latest
if %errorlevel% neq 0 (
    echo Failed to start Docker container!
    exit /b 1
)

echo Container started successfully!
echo Access your application at http://localhost:8080
echo Container logs can be viewed with: docker logs aitbackend-container
echo To stop the container run: docker stop aitbackend-container
