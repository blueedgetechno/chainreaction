xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator -destination "platform=iOS Simulator,name=iPhone 17 Pro" -configuration Debug build
xcrun simctl install "iPhone 17 Pro" ~/Library/Developer/Xcode/DerivedData/iosApp-emryfhatixepiidrvymvyhzquzdk/Build/Products/Debug-iphonesimulator/iosApp.app
xcrun simctl launch "iPhone 17 Pro" com.blueedge.chainreaction