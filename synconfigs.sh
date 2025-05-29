#!/bin/bash
set -e

echo "🔄 [synconfigs.sh] Starting environment files synchronization..."

# Define file paths
ROOT_ENV=".env"
ROOT_ENV_EXAMPLE=".env.example"
ROOT_ENV_LOCAL=".env.local"
DB_ENV="database/.env"
DB_ENV_EXAMPLE="database/.env.example"
DB_ENV_LOCAL="database/.env.local"

# Function to get common variables between two files
get_common_vars() {
    local file1=$1
    local file2=$2

    grep -v '^#' "$file1" | grep -v '^$' | cut -d'=' -f1 | sort > /tmp/vars1
    grep -v '^#' "$file2" | grep -v '^$' | cut -d'=' -f1 | sort > /tmp/vars2

    comm -12 /tmp/vars1 /tmp/vars2
}

# Function to update value in a file
update_value() {
    local file=$1
    local var=$2
    local new_value=$3

    # Escape special characters for safe sed usage
    new_value=$(echo "$new_value" | sed 's/[\/&]/\\&/g')

    # Update the variable's value
    sed -i.bak "s/^${var}=.*/${var}=${new_value}/" "$file"
    rm "${file}.bak"
}

# Function to sync env values from source to target file
sync_env_files() {
    local source_file=$1
    local target_file=$2
    local file_type=$3

    echo "📂 [synconfigs.sh] Syncing $file_type files..."

    # Warn if file is missing, don't exit
    if [ ! -f "$source_file" ]; then
        echo "⚠️ [synconfigs.sh] Warning: Source file '$source_file' not found. Skipping..."
        return
    fi
    if [ ! -f "$target_file" ]; then
        echo "⚠️ [synconfigs.sh] Warning: Target file '$target_file' not found. Skipping..."
        return
    fi

    # Get and sync common variables
    COMMON_VARS=$(get_common_vars "$source_file" "$target_file")
    var_count=$(echo "$COMMON_VARS" | wc -l)
    echo "📊 [synconfigs.sh] Found $var_count common variables to sync"

    for var in $COMMON_VARS; do
        value=$(grep "^${var}=" "$source_file" | cut -d'=' -f2-)
        if [ -n "$value" ]; then
            update_value "$target_file" "$var" "$value"
            echo "  ✅ [synconfigs.sh] Synced $var"
        fi
    done
}

# Sync all file types
sync_env_files "$ROOT_ENV" "$DB_ENV" ".env"
sync_env_files "$ROOT_ENV_EXAMPLE" "$DB_ENV_EXAMPLE" ".env.example"
sync_env_files "$ROOT_ENV_LOCAL" "$DB_ENV_LOCAL" ".env.local"

echo "✅ [synconfigs.sh] Environment files synchronized successfully!"
