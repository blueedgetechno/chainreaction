#!/bin/bash
set -e

echo "📤 Uploading IPA to Firebase App Distribution..."
npm install -g firebase-tools
firebase appdistribution:distribute iosApp.ipa \
  --app "$IOS_APP_ID" \
  --token "$FIREBASE_TOKEN" \
  --release-notes "$RELEASE_NOTES" \
  --groups "phase1"
echo "✅ Upload complete."
