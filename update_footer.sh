#!/bin/bash

# This script updates the HTML templates to replace hardcoded footers with the footer fragment

# Loop through all HTML files in user templates
find backend/src/main/resources/templates/user -type f -name "*.html" | while read file; do
    echo "Processing $file..."
    
    # Check if the file has a hardcoded footer
    if grep -q '<footer class="bg-dark' "$file"; then
        # Replace the hardcoded footer with the fragment
        sed -i '' -e '/<footer class="bg-dark/,/<\/footer>/c\
    <footer th:replace="~{shared/fragments/footer :: footer}"></footer>' "$file"
        echo "Updated $file"
    else
        echo "No hardcoded footer found in $file, skipping"
    fi
done

echo "Footer update completed!" 