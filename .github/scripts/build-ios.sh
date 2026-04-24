#!/bin/bash
set -e

echo "🛠️ Building iOS IPA (Release)..."

# Ensure Gradle wrapper is executable
chmod +x ./gradlew

# Build the shared Kotlin framework for iOS (arm64 device target)
echo "📦 Building shared Kotlin framework..."
./gradlew :shared:linkReleaseFrameworkIosArm64

# Archive the Xcode project
echo "📱 Archiving Xcode project..."
xcodebuild archive \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -sdk iphoneos \
  -configuration Release \
  -archivePath "$GITHUB_WORKSPACE/iosApp.xcarchive" \
  CODE_SIGN_IDENTITY="$CODE_SIGN_IDENTITY" \
  DEVELOPMENT_TEAM="$DEVELOPMENT_TEAM" \
  PROVISIONING_PROFILE_SPECIFIER="$PROVISIONING_PROFILE_SPECIFIER" \
  CODE_SIGN_STYLE=Manual

echo "✅ iOS archive complete."
