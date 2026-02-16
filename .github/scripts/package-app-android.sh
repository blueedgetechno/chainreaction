#!/bin/bash
ls ./app/build/outputs/apk/release

cd ./app/build/outputs/apk/release
mv app-release.apk "$GITHUB_WORKSPACE/"
