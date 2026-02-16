# TODO

- [ ] Replace debug signing config with a production release keystore for Play Store / production distribution. Currently `app/build.gradle.kts` uses `signingConfigs.getByName("debug")` for the release build type.
