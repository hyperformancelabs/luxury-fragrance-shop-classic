#!/bin/bash
set -e

# =======================
# 🧩 Shared Utilities
# =======================

confirm_action() {
    local prompt="$1"
    while true; do
        read -p "❓ $prompt (yes/no): " confirm
        confirm=$(echo "$confirm" | tr '[:upper:]' '[:lower:]')
        if [[ "$confirm" == "yes" ]]; then
            return 0
        elif [[ "$confirm" == "no" ]]; then
            echo "❌ Action cancelled."
            return 1
        else
            echo "⚠️  Please type 'yes' or 'no'."
        fi
    done
}

verify_sa_password() {
    local input_pwd="$1"

    if [ -z "$input_pwd" ]; then
        while true; do
            read -s -p "🔐 Enter SA password: " input_pwd
            echo
            echo "🔄 [full-setup.sh] Verifying SA credentials..."
            if docker exec "$CONTAINER_NAME" /opt/mssql-tools/bin/sqlcmd \
                -S localhost \
                -U sa \
                -P "$input_pwd" \
                -Q "SELECT 1" -h -1 > /dev/null 2>&1; then
                MSSQL_SA_PASSWORD="$input_pwd"
                return
            else
                echo "❌ Incorrect SA password. Try again."
            fi
        done
    else
        echo "🔄 [full-setup.sh] Verifying SA credentials..."
        if docker exec "$CONTAINER_NAME" /opt/mssql-tools/bin/sqlcmd \
            -S localhost \
            -U sa \
            -P "$input_pwd" \
            -Q "SELECT 1" -h -1 > /dev/null 2>&1; then
            MSSQL_SA_PASSWORD="$input_pwd"
            return
        else
            echo "❌ Incorrect SA password from environment file."
            verify_sa_password ""
        fi
    fi
}

# =======================
# 🚀 Script Bootstrap
# =======================

echo "🚀 [full-setup.sh] Starting full database setup..."

# Detect environment file
ENV_FILE=""
if [ -f ".env.local" ]; then
    ENV_FILE=".env.local"
    echo "📄 Found .env.local file."
elif [ -f ".env" ]; then
    ENV_FILE=".env"
    echo "📄 Found .env file."
else
    echo "⚠️  No environment file found. Will prompt for values manually."
fi

# Load environment variables
if [ -n "$ENV_FILE" ]; then
    echo "📂 Loading environment variables from $ENV_FILE..."
    export $(grep -v '^#' "$ENV_FILE" | xargs)
fi

# Check if container is running
if [ -z "$CONTAINER_NAME" ]; then
    read -p "📦 Enter container name: " CONTAINER_NAME
fi

if ! docker ps | grep -q "$CONTAINER_NAME"; then
    echo "❌ Container '$CONTAINER_NAME' is not running!"
    echo "🛠️  Please start it first using: ./start.sh"
    exit 1
fi

# Verify SA password
verify_sa_password "$MSSQL_SA_PASSWORD"

# =======================
# 🛠️  Setup Database
# =======================

# Get database name
if [ -z "$MSSQL_DATABASE" ]; then
    read -p "🗄️  Enter database name: " MSSQL_DATABASE
fi

echo "🛠️  Creating database '$MSSQL_DATABASE'..."
docker exec $CONTAINER_NAME /opt/mssql-tools/bin/sqlcmd \
    -S localhost \
    -U sa \
    -P "$MSSQL_SA_PASSWORD" \
    -Q "IF DB_ID('$MSSQL_DATABASE') IS NULL BEGIN CREATE DATABASE [$MSSQL_DATABASE]; PRINT('✅ Database $MSSQL_DATABASE created.'); END ELSE PRINT('ℹ️ Database $MSSQL_DATABASE already exists.');"

# =======================
# 👥 Setup User
# =======================

# Get username
if [ -z "$MSSQL_USERNAME" ]; then
    read -p "👤 Enter new username: " MSSQL_USERNAME
fi

# Get password
if [ -z "$MSSQL_PASSWORD" ]; then
    read -s -p "🔑 Enter password for $MSSQL_USERNAME: " MSSQL_PASSWORD
    echo
fi

echo "👤 Creating login '$MSSQL_USERNAME'..."
docker exec $CONTAINER_NAME /opt/mssql-tools/bin/sqlcmd \
    -S localhost \
    -U sa \
    -P "$MSSQL_SA_PASSWORD" \
    -Q "IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = '$MSSQL_USERNAME') BEGIN CREATE LOGIN [$MSSQL_USERNAME] WITH PASSWORD = '$MSSQL_PASSWORD'; PRINT('✅ Login $MSSQL_USERNAME created.'); END ELSE PRINT('ℹ️ Login $MSSQL_USERNAME already exists.');"

echo "🛡️  Setting up database user and permissions..."
docker exec $CONTAINER_NAME /opt/mssql-tools/bin/sqlcmd \
    -S localhost \
    -U sa \
    -P "$MSSQL_SA_PASSWORD" \
    -Q "USE [$MSSQL_DATABASE]; IF EXISTS (SELECT * FROM sys.database_principals WHERE name = '$MSSQL_USERNAME') BEGIN DROP USER [$MSSQL_USERNAME]; END; CREATE USER [$MSSQL_USERNAME] FOR LOGIN [$MSSQL_USERNAME]; EXEC sp_addrolemember 'db_owner', '$MSSQL_USERNAME'; PRINT('✅ User $MSSQL_USERNAME set up with full access to $MSSQL_DATABASE.');"

# =======================
# 🔍 Verification
# =======================

echo "🔍 Verifying setup..."

# Test connection with new user
echo "🔌 Testing connection with '$MSSQL_USERNAME'..."
if docker exec $CONTAINER_NAME /opt/mssql-tools/bin/sqlcmd \
    -S localhost \
    -U "$MSSQL_USERNAME" \
    -P "$MSSQL_PASSWORD" \
    -d "$MSSQL_DATABASE" \
    -Q "SELECT 'Connection successful'" -h -1 > /dev/null 2>&1; then
    echo "✅ Connection test successful!"
else
    echo "❌ Connection test failed!"
    exit 1
fi

# Show database information
echo -e "\n📊 Database Info:"

echo -e "\n📦 Database:"
docker exec $CONTAINER_NAME /opt/mssql-tools/bin/sqlcmd \
    -S localhost \
    -U sa \
    -P "$MSSQL_SA_PASSWORD" \
    -Q "SELECT name, create_date FROM sys.databases WHERE name = '$MSSQL_DATABASE';" -h -1 -W

echo -e "\n👥 Login:"
docker exec $CONTAINER_NAME /opt/mssql-tools/bin/sqlcmd \
    -S localhost \
    -U sa \
    -P "$MSSQL_SA_PASSWORD" \
    -Q "SELECT name, type_desc, create_date FROM sys.server_principals WHERE name = '$MSSQL_USERNAME';" -h -1 -W

echo -e "\n👤 Database User:"
docker exec $CONTAINER_NAME /opt/mssql-tools/bin/sqlcmd \
    -S localhost \
    -U sa \
    -P "$MSSQL_SA_PASSWORD" \
    -Q "USE [$MSSQL_DATABASE]; SELECT dp.name, dp.type_desc, OBJECT_NAME(drm.role_principal_id) as role_name FROM sys.database_principals dp JOIN sys.database_role_members drm ON dp.principal_id = drm.member_principal_id WHERE dp.name = '$MSSQL_USERNAME';" -h -1 -W

# Finish
echo -e "\n🎯 Setup Completed Successfully!"
echo "🔐 Connect info:"
echo "  - Server: localhost"
echo "  - Port: ${MSSQL_PORT:-1433}"
echo "  - Database: $MSSQL_DATABASE"
echo "  - Username: $MSSQL_USERNAME"
echo "  - Password: [your input]"
