#!/bin/bash


#Functoin to build the project

build_project() {
  PROJECT_DIR=$1
  echo "Building project in $PROJECT_DIR"
  
  cd $PROJECT_DIR || { echo "Failed to change directory to $PROJECT_DIR"; exit 1; }

  chmod +x ./gradlew

  echo "Cleaning previous builds"
  ./gradlew clean --no-daemon

  echo "Building project"
  ./gradlew build --no-daemon -Dorg.gradle.daemon=false

  cd -> /dev/null || echo "Failed to change directory to /dev/null" && exit 1
  echo "Build completed for $PROJECT_DIR"
}

PROJECTS=("amine-api-app" "authentication-app")

for PROJECT in "${PROJECTS[@]}"; do
  build_project "$PROJECT"
done

# Check if the build was successful
if [ $? -eq 0 ]; then
  echo "All projects built successfully."
else
  echo "Build failed for one or more projects."
  exit 1
fi