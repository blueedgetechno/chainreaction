#!/bin/bash
cd /app/build/outputs/apk/release
mv app-release.apk "$GITHUB_WORKSPACE/"
