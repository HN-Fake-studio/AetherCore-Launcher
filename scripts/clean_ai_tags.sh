#!/bin/bash
# ==============================================================================
# Script Name: clean_ai_tags.sh
# Description: Removes all `_(ai)` suffixes from filenames, directory names, and
#              internal references in the AetherCore Project when development is complete.
# ==============================================================================

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
echo "Starting cleanup of '_(ai)' tags in project: $PROJECT_DIR"

cd "$PROJECT_DIR" || exit 1

# 1. Rename files containing '_(ai)'
echo "Renaming files..."
find . -depth -name "*_\(ai\)*" | while IFS= read -r file; do
    dir=$(dirname "$file")
    base=$(basename "$file")
    new_base="${base//_\(ai\)/}"
    new_path="$dir/$new_base"
    echo "Renaming file: $file -> $new_path"
    mv "$file" "$new_path"
done

# 2. Rename directories containing '_(ai)'
echo "Renaming directories..."
find . -depth -name "*_\(ai\)*" | while IFS= read -r dir; do
    parent=$(dirname "$dir")
    base=$(basename "$dir")
    new_base="${base//_\(ai\)/}"
    new_path="$parent/$new_base"
    echo "Renaming directory: $dir -> $new_path"
    mv "$dir" "$new_path"
done

# 3. Replace text references inside source code and configuration files
echo "Cleaning internal code references..."
find . -type f \( -name "*.kt" -name "*.kts" -name "*.xml" -name "*.properties" -name "*.json" -name "*.sh" \) | while IFS= read -r src; do
    if grep -q "_\(ai\)" "$src"; then
        echo "Updating references in: $src"
        sed -i 's/_\(ai\)//g' "$src"
    fi
done

echo "Cleanup completed successfully! All '_(ai)' tags have been removed."
