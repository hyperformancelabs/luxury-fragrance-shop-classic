#!/bin/bash
set -e

# ======================
# 🧹 Shared utilities
# ======================

confirm_action() {
    local prompt="$1"
    while true; do
        read -p "$prompt (yes/no): " confirm
        confirm=$(echo "$confirm" | tr '[:upper:]' '[:lower:]')
        if [[ "$confirm" == "yes" ]]; then
            return 0
        elif [[ "$confirm" == "no" ]]; then
            echo "❌ Action cancelled."
            return 1
        else
            echo "❌ Please type 'yes' or 'no'."
        fi
    done
}

prompt_with_default() {
    local prompt="$1"
    local default_value="$2"
    read -p "$prompt (default: $default_value): " input
    echo "${input:-$default_value}"
}

verify_sa_password() {
    local input_pwd="$MSSQL_SA_PASSWORD"

    if [ -z "$input_pwd" ]; then
        while true; do
            read -s -p "🔐 Enter SA password: " input_pwd
            echo
            echo "🛡️ Verifying SA credentials..."
            if docker exec "$CONTAINER_NAME" /opt/mssql-tools/bin/sqlcmd \
                -S localhost -U sa -P "$input_pwd" -Q "SELECT 1" -h -1 > /dev/null 2>&1; then
                MSSQL_SA_PASSWORD="$input_pwd"
                return
            else
                echo "❌ Incorrect password. Please try again."
            fi
        done
    else
        echo "🛡️ Verifying SA credentials..."
        if ! docker exec "$CONTAINER_NAME" /opt/mssql-tools/bin/sqlcmd \
            -S localhost -U sa -P "$input_pwd" -Q "SELECT 1" -h -1 > /dev/null 2>&1; then
            echo "❌ Incorrect password from env file."
            verify_sa_password
        fi
    fi
}

# =======================
# 🎯 Core actions
# =======================

create_database() {
    verify_sa_password
    local db_name=$(prompt_with_default "📦 Enter database name" "$MSSQL_DATABASE")
    echo "📦 Creating database '$db_name'..."

    docker exec "$CONTAINER_NAME" /opt/mssql-tools/bin/sqlcmd \
        -S localhost -U sa -P "$MSSQL_SA_PASSWORD" \
        -Q "IF DB_ID('$db_name') IS NULL BEGIN CREATE DATABASE [$db_name]; PRINT('✅ Database $db_name created.'); END ELSE PRINT('ℹ️ Database $db_name already exists.');"
}

delete_database() {
    verify_sa_password
    local db_name=$(prompt_with_default "🗑️ Enter database name to delete" "$MSSQL_DATABASE")
    confirm_action "⚠️ This will permanently delete database '$db_name'" || return

    echo "🗑️ Deleting database '$db_name'..."
    docker exec "$CONTAINER_NAME" /opt/mssql-tools/bin/sqlcmd \
        -S localhost -U sa -P "$MSSQL_SA_PASSWORD" \
        -Q "IF DB_ID('$db_name') IS NOT NULL BEGIN ALTER DATABASE [$db_name] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; DROP DATABASE [$db_name]; PRINT('✅ Database $db_name deleted.'); END ELSE PRINT('ℹ️ Database $db_name does not exist.');"
}

create_user_login() {
    verify_sa_password
    local username=$(prompt_with_default "👤 Enter login username" "$MSSQL_USERNAME")
    local password=$(prompt_with_default "🔑 Enter login password" "$MSSQL_PASSWORD")

    if [ "$username" = "sa" ]; then
        echo "❌ Cannot create or overwrite 'sa' user."
        exit 1
    fi

    echo "👤 Creating server login '$username'..."
    docker exec "$CONTAINER_NAME" /opt/mssql-tools/bin/sqlcmd \
        -S localhost -U sa -P "$MSSQL_SA_PASSWORD" \
        -Q "IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = '$username') BEGIN CREATE LOGIN [$username] WITH PASSWORD = '$password'; PRINT('✅ Login $username created.'); END ELSE PRINT('ℹ️ Login $username already exists.');"
}

create_user_database() {
    verify_sa_password
    local username=$(prompt_with_default "👤 Enter database username (login name)" "$MSSQL_USERNAME")
    local db_name=$(prompt_with_default "📦 Enter database name" "$MSSQL_DATABASE")

    echo "👤 Creating database user '$username' in '$db_name'..."
    docker exec "$CONTAINER_NAME" /opt/mssql-tools/bin/sqlcmd \
        -S localhost -U sa -P "$MSSQL_SA_PASSWORD" \
        -Q "USE [$db_name]; \
            IF EXISTS (SELECT * FROM sys.database_principals WHERE name = '$username') BEGIN DROP USER [$username]; END; \
            CREATE USER [$username] FOR LOGIN [$username]; \
            EXEC sp_addrolemember 'db_owner', '$username'; \
            PRINT('✅ Database user $username created and assigned db_owner role.');"
}

delete_user() {
    verify_sa_password
    local username=$(prompt_with_default "🗑️ Enter login username to delete" "$MSSQL_USERNAME")

    if [ "$username" = "sa" ]; then
        echo "❌ Cannot delete 'sa' user."
        return 1
    fi

    confirm_action "⚠️ This will permanently delete login '$username'" || return

    # Check nếu user đang đăng nhập
    echo "🔍 Checking active sessions for '$username'..."
    local session_count=$(docker exec "$CONTAINER_NAME" /opt/mssql-tools/bin/sqlcmd \
        -S localhost -U sa -P "$MSSQL_SA_PASSWORD" \
        -h -1 -W -Q "SELECT COUNT(*) FROM sys.sysprocesses WHERE loginame = '$username';")

    session_count=$(echo "$session_count" | tr -d '\r')  # Xử lý bỏ ký tự carriage return (nếu có)

    if [ "$session_count" != "0" ]; then
        echo "⚠️ User '$username' has $session_count active session(s)."
        confirm_action "⚠️ User is currently logged in. Force logout all sessions?" || return

        echo "🛑 Logging out sessions for '$username'..."
        docker exec "$CONTAINER_NAME" /opt/mssql-tools/bin/sqlcmd \
            -S localhost -U sa -P "$MSSQL_SA_PASSWORD" \
            -Q "DECLARE @spid INT
                WHILE EXISTS (SELECT * FROM sys.sysprocesses WHERE loginame = '$username')
                BEGIN
                    SELECT TOP 1 @spid = spid FROM sys.sysprocesses WHERE loginame = '$username'
                    EXEC('KILL ' + @spid)
                END"
    fi

    echo "🗑️ Deleting login '$username'..."
    docker exec "$CONTAINER_NAME" /opt/mssql-tools/bin/sqlcmd \
        -S localhost -U sa -P "$MSSQL_SA_PASSWORD" \
        -Q "IF EXISTS (SELECT * FROM sys.server_principals WHERE name = '$username')
            BEGIN
                DROP LOGIN [$username];
                PRINT('✅ Login $username deleted.');
            END
            ELSE PRINT('ℹ️ Login $username does not exist.');"
}


modify_user_permissions() {
    verify_sa_password
    local username=$(prompt_with_default "🔑 Enter database username" "$MSSQL_USERNAME")
    local db_name=$(prompt_with_default "📦 Enter database name" "$MSSQL_DATABASE")

    echo "🔧 Modifying permissions for '$username' in '$db_name'..."
    confirm_action "⚠️ This will overwrite permissions for user '$username' in '$db_name'" || return

    echo "Available permissions: SELECT INSERT UPDATE DELETE EXECUTE VIEW DEFINITION"
    read -p "Enter permissions (space separated, default all): " input
    local permissions
    if [ -z "$input" ]; then
        permissions="SELECT, INSERT, UPDATE, DELETE, EXECUTE, VIEW DEFINITION"
    else
        permissions=$(echo "$input" | sed 's/ /, /g')
    fi

    docker exec "$CONTAINER_NAME" /opt/mssql-tools/bin/sqlcmd \
        -S localhost -U sa -P "$MSSQL_SA_PASSWORD" \
        -Q "USE [$db_name]; IF EXISTS (SELECT * FROM sys.database_principals WHERE name = '$username') BEGIN DROP USER [$username]; END; CREATE USER [$username] FOR LOGIN [$username]; GRANT $permissions TO [$username]; PRINT('✅ Permissions updated.');"
}

change_user_password() {
    verify_sa_password
    local username=$(prompt_with_default "👤 Enter login username" "$MSSQL_USERNAME")
    read -s -p "🔑 Enter new password: " new_password
    echo

    if [ "$username" = "sa" ]; then
        echo "❌ Cannot change password for 'sa' user."
        exit 1
    fi

    confirm_action "⚠️ Are you sure you want to change password for '$username'?" || return

    docker exec "$CONTAINER_NAME" /opt/mssql-tools/bin/sqlcmd \
        -S localhost -U sa -P "$MSSQL_SA_PASSWORD" \
        -Q "ALTER LOGIN [$username] WITH PASSWORD = '$new_password'; PRINT('✅ Password updated.');"
}

show_db_info() {
    verify_sa_password
    echo "🔍 Fetching database info..."

    echo -e "\n📦 Version:"
    docker exec "$CONTAINER_NAME" /opt/mssql-tools/bin/sqlcmd \
        -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -Q "SELECT @@VERSION;" -h -1 -W

    echo -e "\n📚 Databases:"
    docker exec "$CONTAINER_NAME" /opt/mssql-tools/bin/sqlcmd \
        -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -Q "SELECT name FROM sys.databases WHERE database_id > 4;" -h -1 -W -s "," | column -t -s ','

    echo -e "\n👥 Logins:"
    docker exec "$CONTAINER_NAME" /opt/mssql-tools/bin/sqlcmd \
        -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -Q "SELECT name, type_desc FROM sys.server_principals WHERE type_desc = 'SQL_LOGIN' AND name NOT IN ('sa');" -h -1 -W -s "," | column -t -s ','
}

# ======================
# 🚀 Script bootstrap
# ======================

ENV_FILE=""
for (( i=1; i<=$#; i++ )); do
    arg=${!i}
    if [ "$arg" = "--env" ]; then
        next=$((i + 1))
        ENV_FILE=${!next}
        break
    fi
done

if [ -z "$ENV_FILE" ]; then
    if [ -f ".env.local" ]; then ENV_FILE=".env.local"
    elif [ -f ".env" ]; then ENV_FILE=".env"
    else echo "❌ No env file found!"; exit 1; fi
fi

echo "🛠️ Using env file: $ENV_FILE"
export $(grep -v '^#' "$ENV_FILE" | xargs)

REQUIRED_VARS=(MSSQL_SA_PASSWORD CONTAINER_NAME)
for var in "${REQUIRED_VARS[@]}"; do
    [ -z "${!var}" ] && echo "❌ Missing variable: $var" && exit 1
done

# ======================
# 🧱️ CLI entrypoint
# ======================

if [ $# -eq 0 ]; then
    while true; do
        clear
        echo -e "\n📋 [1mDatabase Management Menu[0m"
        echo "------------------------------------------"
        echo "1. 📦   Create Database"
        echo "2. 🗑️   Delete Database"
        echo "3. 👤   Create User Login"
        echo "4. 📦👤 Create Database User"
        echo "5. 🗑️   Delete User"
        echo "6. 🔧   Modify User Permissions"
        echo "7. 🔐   Change User Password"
        echo "8. 🔍   Show Database Info"
        echo "9. 👋   Exit"
        echo "------------------------------------------"
        read -p "✨ Choose option (1-9): " choice
        case $choice in
            1) create_database ;;
            2) delete_database ;;
            3) create_user_login ;;
            4) create_user_database ;;
            5) delete_user ;;
            6) modify_user_permissions ;;
            7) change_user_password ;;
            8) show_db_info ;;
            9) echo "👋 Goodbye!"; exit 0 ;;
            *) echo "❌ Invalid option."; sleep 1;;
        esac
        read -n 1 -s -r -p $'\nPress any key to return to menu...'
    done
else
    case "$1" in
        --create-db) create_database ;;
        --delete-db) delete_database ;;
        --create-user-login) create_user_login ;;
        --create-user-db) create_user_database ;;
        --delete-user) delete_user ;;
        --modify-user) modify_user_permissions ;;
        --change-password) change_user_password ;;
        --show-info) show_db_info ;;
        --help|*)
            echo "\n📖 [1mUsage[0m"
            echo "------------------------------------------"
            echo "--create-db              📦   Create Database"
            echo "--delete-db              🗑️   Delete Database"
            echo "--create-user-login      👤   Create Login"
            echo "--create-user-db         📦👤 Create Database User"
            echo "--delete-user            🗑️   Delete User"
            echo "--modify-user            🔧   Modify Permissions"
            echo "--change-password        🔐   Change Password"
            echo "--show-info              🔍   Show Info"
            echo "--env [file]             Specify env file"
            echo "--help                   Show Help"
            echo "------------------------------------------"
            ;;
    esac
fi
