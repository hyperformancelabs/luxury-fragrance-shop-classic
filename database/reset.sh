#!/bin/bash
set -e

ENV_FILE=${1:-}

# Auto-detect env file
if [ -z "$ENV_FILE" ]; then
  if [ -f ".env.local" ]; then ENV_FILE=".env.local"
  elif [ -f ".env" ]; then ENV_FILE=".env"
  else echo "❌ No env file found!"; exit 1; fi
fi

echo "♻️ [reset.sh] Using env file: $ENV_FILE"

# Thông báo và xác nhận với người dùng
echo "⚠️ [reset.sh] WARNING: This will reset your database. All data will be lost!"
echo "❗ [reset.sh] This action is irreversible and cannot be undone!"

while true; do
  read -p "Are you sure you want to continue? (yes/no): " confirmation
  confirmation=$(echo "$confirmation" | tr '[:upper:]' '[:lower:]')  # Chuyển về lowercase

  if [[ "$confirmation" == "yes" || "$confirmation" == "no" ]]; then
    break
  else
    echo "❌ Please type 'yes' or 'no'."
  fi
done

if [[ "$confirmation" == "yes" ]]; then
  echo "♻️ [reset.sh] Resetting database..."
  docker-compose --env-file "$ENV_FILE" down -v

  echo "🚀 [reset.sh] Restarting database..."
  docker-compose --env-file "$ENV_FILE" up -d --build

  echo "✅ [reset.sh] Database has been reset!"
else
  echo "❌ [reset.sh] Reset cancelled."
fi
