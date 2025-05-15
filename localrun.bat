@echo off
echo Building Docker image for aitbackend...
docker build -t aitbackend:latest .

echo Running Docker container with GCP SQL connection...
docker run -p 8080:8080 --name aitbackend-container aitbackend:latest

echo Container started! Access your application at http://localhost:8080
