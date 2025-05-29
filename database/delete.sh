#!/bin/bash
set -e

ENV_FILE=${1:-}

# Auto-detect env file
if [ -z "$ENV_FILE" ]; then
  if [ -f ".env.local" ]; then ENV_FILE=".env.local"
  elif [ -f ".env" ]; then ENV_FILE=".env"
  else echo "❌ No env file found!"; exit 1; fi
fi

echo "🗑️ [delete.sh] Using env file: $ENV_FILE"
export $(grep -v '^#' "$ENV_FILE" | xargs)

# Kiểm tra biến CONTAINER_NAME
[ -z "$CONTAINER_NAME" ] && echo "❌ Missing variable: CONTAINER_NAME" && exit 1

# Thông báo và xác nhận với người dùng
echo "⚠️ [delete.sh] WARNING: This will completely remove the database container and all data!"
echo "❗ [delete.sh] This action is irreversible and cannot be undone!"

while true; do
  read -p "Are you sure you want to continue? (yes/no): " confirmation
  confirmation=$(echo "$confirmation" | tr '[:upper:]' '[:lower:]')  # Chuyển về chữ thường

  if [[ "$confirmation" == "yes" || "$confirmation" == "no" ]]; then
    break
  else
    echo "❌ Please type 'yes' or 'no'."
  fi
done

if [[ "$confirmation" == "yes" ]]; then
  echo "🗑️ [delete.sh] Stopping and removing database..."
  docker-compose --env-file "$ENV_FILE" down -v
  
  # Xóa image nếu có
  if docker images | grep -q "$CONTAINER_NAME"; then
    echo "🗑️ [delete.sh] Removing related Docker images..."
    docker rmi $(docker images | grep "$CONTAINER_NAME" | awk '{print $3}')
  fi
  
  echo "✅ [delete.sh] Database has been completely removed!"
else
  echo "❌ [delete.sh] Deletion cancelled."
fi
