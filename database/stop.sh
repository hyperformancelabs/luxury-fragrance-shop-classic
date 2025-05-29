#!/bin/bash
set -e

ENV_FILE=${1:-}

# Auto-detect env file
if [ -z "$ENV_FILE" ]; then
  if [ -f ".env.local" ]; then ENV_FILE=".env.local"
  elif [ -f ".env" ]; then ENV_FILE=".env"
  else echo "❌ No env file found!"; exit 1; fi
fi

echo "🛑 [stop.sh] Using env file: $ENV_FILE"

# Stop database container
echo "🛑 [stop.sh] Stopping database container..."
docker-compose --env-file $ENV_FILE down

echo "✅ [stop.sh] Database stopped!"
