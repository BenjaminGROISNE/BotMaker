param(
    [Parameter(Mandatory=$true)]
    [string]$SourceDir,

    [string]$OutputFile = "java_files_concat.txt"
)

# Check if directory exists
if (!(Test-Path -Path $SourceDir -PathType Container)) {
    Write-Host "Error: Directory '$SourceDir' does not exist"
    exit 1
}

# Remove output file if it exists
if (Test-Path $OutputFile) {
    Remove-Item $OutputFile
    Write-Host "Removed existing output file: $OutputFile"
}

$fileCount = 0

# Get all .java files recursively
$javaFiles = Get-ChildItem -Path $SourceDir -Recurse -Filter *.java

foreach ($file in $javaFiles) {

    # Compute relative path by removing the prefix directory
    $relPath = $file.FullName.Substring($SourceDir.Length).TrimStart("\","/")

    # Write header
    Add-Content -Path $OutputFile -Value ("=" * 81)
    Add-Content -Path $OutputFile -Value ("FILE: $relPath")
    Add-Content -Path $OutputFile -Value ("=" * 81)
    Add-Content -Path $OutputFile -Value ""

    # Write file content
    Get-Content $file.FullName | Add-Content -Path $OutputFile

    # Spacing
    Add-Content -Path $OutputFile -Value ""
    Add-Content -Path $OutputFile -Value ""

    $fileCount++
}

# Summary
if ($fileCount -eq 0) {
    Write-Host "No Java files found in '$SourceDir'"
    if (Test-Path $OutputFile) { Remove-Item $OutputFile }
    exit 0
} else {
    Write-Host "Successfully concatenated $fileCount Java file(s)"
    Write-Host "Output saved to: $OutputFile"

    # Show file size
    $size = (Get-Item $OutputFile).Length / 1KB
    Write-Host ("Output file size: {0:N2} KB" -f $size)
}
