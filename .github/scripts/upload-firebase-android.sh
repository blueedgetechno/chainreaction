#!/bin/bash
set -e
# Required: ANDROID_APP_ID and FIREBASE_TOKEN must be passed in via environment variables
echo "📤 Uploading APK to Firebase App Distribution..."
npm install -g firebase-tools 
firebase appdistribution:distribute app-release.apk \
  --app "$ANDROID_APP_ID" \
  --token "$FIREBASE_TOKEN" \
  --release-notes "$RELEASE_NOTES" \
  --groups "phase1"
echo "✅ Upload complete."
