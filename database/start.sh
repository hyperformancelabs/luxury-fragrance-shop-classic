#!/bin/bash
set -e

ENV_FILE=${1:-}

# Auto-detect env file
if [ -z "$ENV_FILE" ]; then
  if [ -f ".env.local" ]; then ENV_FILE=".env.local"
  elif [ -f ".env" ]; then ENV_FILE=".env"
  else echo "❌ No env file found!"; exit 1; fi
fi

echo "🚀 [start.sh] Using env file: $ENV_FILE"

# Start database container
echo "🚀 [start.sh] Starting database container..."
docker-compose --env-file $ENV_FILE up -d --build

echo "✅ [start.sh] Database started! You can check logs with: docker logs $(grep CONTAINER_NAME $ENV_FILE | cut -d'=' -f2)"
