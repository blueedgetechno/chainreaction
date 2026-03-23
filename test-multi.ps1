# ── test-multi.ps1 ──────────────────────────────────────────
# Launches two emulators, builds the debug APK, installs it
# on both, and opens the main activity on each.
# Usage:  .\test-multi.ps1
# ────────────────────────────────────────────────────────────
param (
    [switch]$Yes
)

$AVD1 = "Pixel_8_API_33"
$AVD2 = "Pixel_8_API_33_2"
$PACKAGE = "com.blueedge.chainreaction"
$ACTIVITY = "$PACKAGE/.MainActivity"
$APK_PATH = "app\build\outputs\apk\debug\app-debug.apk"

function Get-OnlineSerials {
    $lines = adb devices | Where-Object { $_ -match "^emulator-\d+\s+device" }
    $serials = @()
    foreach ($line in $lines) {
        $serials += ($line -split "\s+")[0]
    }
    return $serials
}

# ── 1. Check existing emulators / launch if needed ─────────
Write-Host "`n=== Checking emulators ===" -ForegroundColor Cyan

$existing = Get-OnlineSerials
$existingCount = @($existing).Count

if ($existingCount -ge 2) {
    Write-Host "  $existingCount emulators already running: $($existing -join ', ')" -ForegroundColor Green
    Write-Host "  Skipping emulator launch."
} else {
    Write-Host "  $existingCount emulator(s) online — need 2. Launching..."
    
    # Check which AVDs are already running via their ports
    $runningAvds = @()
    foreach ($s in $existing) {
        $runningAvds += $s
    }

    if ($existingCount -eq 0) {
        Write-Host "  Starting $AVD1..."
        Start-Process -FilePath "emulator" -ArgumentList "-avd $AVD1 -no-snapshot-load" -WindowStyle Minimized
        Write-Host "  Starting $AVD2..."
        Start-Process -FilePath "emulator" -ArgumentList "-avd $AVD2 -no-snapshot-load" -WindowStyle Minimized
    } elseif ($existingCount -eq 1) {
        Write-Host "  Starting $AVD2..."
        Start-Process -FilePath "emulator" -ArgumentList "-avd $AVD2 -no-snapshot-load" -WindowStyle Minimized
    }

    # Wait for both to boot
    Write-Host "Waiting for emulators to come online..."
    $elapsed = 0
    $timeout = 180
    while ($elapsed -lt $timeout) {
        $online = @(Get-OnlineSerials).Count
        Write-Host "  [$elapsed`s] $online / 2 devices online"
        if ($online -ge 2) { break }
        Start-Sleep -Seconds 5
        $elapsed += 5
    }
    if ($elapsed -ge $timeout) {
        Write-Host "Timed out waiting for devices!" -ForegroundColor Red
        exit 1
    }

    # Wait for boot_completed
    $serials = Get-OnlineSerials
    foreach ($serial in $serials) {
        Write-Host "Waiting for $serial to finish booting..."
        $booted = $false
        for ($i = 0; $i -lt 60; $i++) {
            $prop = adb -s $serial shell getprop sys.boot_completed 2>$null
            if ($prop -and $prop.Trim() -eq "1") { $booted = $true; break }
            Start-Sleep -Seconds 2
        }
        if ($booted) { Write-Host "  $serial booted!" -ForegroundColor Green }
        else { Write-Host "  $serial boot timed out" -ForegroundColor Yellow }
    }
}

# ── 2. Build debug APK (ask if already exists) ────────────
if (Test-Path $APK_PATH) {
    Write-Host "`nAPK already exists at $APK_PATH" -ForegroundColor Yellow
    $rebuild = $Yes
    
    if (-not $rebuild) {
        $input = Read-Host "Rebuild? (y/N)"
        $rebuild = $input -eq "y" -or $input -eq "Y"
    }

    if ($rebuild) {
        Write-Host "`n=== Building debug APK ===" -ForegroundColor Cyan
        & .\gradlew.bat assembleDebug
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Build failed!" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "Using existing APK."
    }
} else {
    Write-Host "`n=== Building debug APK ===" -ForegroundColor Cyan
    & .\gradlew.bat assembleDebug
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Build failed!" -ForegroundColor Red
        exit 1
    }
}

if (-not (Test-Path $APK_PATH)) {
    Write-Host "APK not found at $APK_PATH" -ForegroundColor Red
    exit 1
}
Write-Host "APK ready: $APK_PATH" -ForegroundColor Green

# ── 3. Install & launch on all devices ────────────────────
Write-Host "`n=== Installing on all devices ===" -ForegroundColor Cyan

$serials = Get-OnlineSerials

foreach ($serial in $serials) {
    Write-Host "Installing on $serial..."
    adb -s $serial install -r $APK_PATH
    Write-Host "Launching on $serial..."
    adb -s $serial shell am start -n $ACTIVITY
    Write-Host "  Done: $serial" -ForegroundColor Green
}

Write-Host "`n=== All done! $($serials.Count) devices ready ===" -ForegroundColor Cyan
