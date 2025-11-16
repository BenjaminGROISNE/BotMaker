#!/bin/bash

# Script to concatenate all Java files from a directory recursively
# Usage: ./concat_java.sh <directory_path> [output_file]

# Check if directory argument is provided
if [ $# -eq 0 ]; then
    echo "Error: No directory specified"
    echo "Usage: $0 <directory_path> [output_file]"
    exit 1
fi

# Get the directory path from argument
SOURCE_DIR="$1"

# Set output file (use second argument or default)
OUTPUT_FILE="${2:-java_files_concat.txt}"

# Check if directory exists
if [ ! -d "$SOURCE_DIR" ]; then
    echo "Error: Directory '$SOURCE_DIR' does not exist"
    exit 1
fi

# Remove output file if it already exists
if [ -f "$OUTPUT_FILE" ]; then
    rm "$OUTPUT_FILE"
    echo "Removed existing output file: $OUTPUT_FILE"
fi

# Initialize counter
file_count=0

# Find all .java files recursively and process them
while IFS= read -r -d '' file; do
    # Get relative path from source directory
    rel_path="${file#$SOURCE_DIR/}"

    # Add separator and file header
    echo "=================================================================================" >> "$OUTPUT_FILE"
    echo "FILE: $rel_path" >> "$OUTPUT_FILE"
    echo "=================================================================================" >> "$OUTPUT_FILE"
    echo "" >> "$OUTPUT_FILE"

    # Append file content
    cat "$file" >> "$OUTPUT_FILE"

    # Add blank lines for readability
    echo "" >> "$OUTPUT_FILE"
    echo "" >> "$OUTPUT_FILE"

    ((file_count++))
done < <(find "$SOURCE_DIR" -type f -name "*.java" -print0 | sort -z)

# Print summary
if [ $file_count -eq 0 ]; then
    echo "No Java files found in '$SOURCE_DIR'"
    rm "$OUTPUT_FILE" 2>/dev/null
    exit 0
else
    echo "Successfully concatenated $file_count Java file(s)"
    echo "Output saved to: $OUTPUT_FILE"

    # Show file size
    size=$(du -h "$OUTPUT_FILE" | cut -f1)
    echo "Output file size: $size"
fi