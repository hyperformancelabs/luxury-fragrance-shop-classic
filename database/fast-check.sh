#!/bin/bash
set -e

ENV_FILE=${1:-}

# Auto-detect env file
if [ -z "$ENV_FILE" ]; then
  if [ -f ".env.local" ]; then ENV_FILE=".env.local"
  elif [ -f ".env" ]; then ENV_FILE=".env"
  else echo "❌ No env file found!"; exit 1
  fi
fi

echo "🔍 [fast-check.sh] Using env file: $ENV_FILE"
export $(grep -v '^#' "$ENV_FILE" | xargs)

REQUIRED_VARS=(MSSQL_SA_PASSWORD CONTAINER_NAME)
for var in "${REQUIRED_VARS[@]}"; do
  [ -z "${!var}" ] && echo "❌ Missing variable: $var" && exit 1
done

# Kiểm tra container đang chạy
if [ ! "$(docker ps -q -f name=$CONTAINER_NAME)" ]; then
  echo "❌ [fast-check.sh] Container '$CONTAINER_NAME' is not running!"
  exit 1
fi

echo "🔍 [fast-check.sh] Testing database connection..."

if docker exec $CONTAINER_NAME /opt/mssql-tools/bin/sqlcmd \
    -S localhost -U sa -P "$MSSQL_SA_PASSWORD" \
    -Q "SELECT 1" -h -1 -W > /dev/null 2>&1; then

  echo "✅ [fast-check.sh] Connection to database is OK!"

  echo -e "\n📦 Version:"
  docker exec $CONTAINER_NAME /opt/mssql-tools/bin/sqlcmd \
    -S localhost -U sa -P "$MSSQL_SA_PASSWORD" \
    -Q "SELECT @@VERSION;" -h -1 -W

  echo -e "\n📚 Databases:"
  docker exec $CONTAINER_NAME /opt/mssql-tools/bin/sqlcmd \
    -S localhost -U sa -P "$MSSQL_SA_PASSWORD" \
    -Q "SET NOCOUNT ON; SELECT name FROM sys.databases WHERE database_id > 4 ORDER BY name;" \
    -h -1 -W -s "," | column -t -s ','

  echo -e "\n👥 Logins:"
  docker exec $CONTAINER_NAME /opt/mssql-tools/bin/sqlcmd \
    -S localhost -U sa -P "$MSSQL_SA_PASSWORD" \
    -Q "SET NOCOUNT ON; SELECT name, type_desc FROM sys.server_principals WHERE type_desc = 'SQL_LOGIN' AND name NOT IN ('sa', '##MS_PolicyEventProcessingLogin##', '##MS_PolicyTsqlExecutionLogin##') ORDER BY name;" \
    -h -1 -W -s "," | column -t -s ','

  echo -e "\n✅ [fast-check.sh] All checks completed.\n"
  exit 0
else
  echo "❌ [fast-check.sh] Failed to connect to database!"
  exit 1
fi
