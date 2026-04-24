#!/bin/bash
set -e

echo "📦 Exporting IPA from archive..."

# Generate ExportOptions.plist with actual values from environment
cat > "$GITHUB_WORKSPACE/ExportOptions.plist" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>method</key>
    <string>ad-hoc</string>
    <key>teamID</key>
    <string>${DEVELOPMENT_TEAM}</string>
    <key>signingStyle</key>
    <string>manual</string>
    <key>provisioningProfiles</key>
    <dict>
        <key>com.blueedge.chainreaction</key>
        <string>${PROVISIONING_PROFILE_SPECIFIER}</string>
    </dict>
</dict>
</plist>
EOF

xcodebuild -exportArchive \
  -archivePath "$GITHUB_WORKSPACE/iosApp.xcarchive" \
  -exportPath "$GITHUB_WORKSPACE/export" \
  -exportOptionsPlist "$GITHUB_WORKSPACE/ExportOptions.plist"

# Move IPA to workspace root
mv "$GITHUB_WORKSPACE/export/"*.ipa "$GITHUB_WORKSPACE/iosApp.ipa"

echo "✅ IPA exported: iosApp.ipa"
