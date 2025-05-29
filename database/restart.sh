#!/bin/bash
set -e

ENV_FILE=${1:-}

# Auto-detect env file
if [ -z "$ENV_FILE" ]; then
  if [ -f ".env.local" ]; then ENV_FILE=".env.local"
  elif [ -f ".env" ]; then ENV_FILE=".env"
  else echo "❌ No env file found!"; exit 1; fi
fi

echo "🔄 [restart.sh] Using env file: $ENV_FILE"

# Dừng container
echo "🛑 [restart.sh] Stopping database container..."
docker-compose --env-file $ENV_FILE down

# Khởi động lại database với cấu hình mới
echo "🚀 [restart.sh] Starting database container with new configuration..."
docker-compose --env-file $ENV_FILE up -d --build

echo "✅ [restart.sh] Database restarted! You can check logs with: docker logs $(grep CONTAINER_NAME $ENV_FILE | cut -d'=' -f2)" 