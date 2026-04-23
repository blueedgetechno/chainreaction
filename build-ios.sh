# SIM_ID="iPad Air 11-inch (M2)"
# SIM_ID="iPhone 15 Pro"
SIM_ID="iPad Pro 13-inch (M5)"

echo "platform=iOS Simulator,name=$SIM_ID"

xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator -destination "platform=iOS Simulator,name=$SIM_ID" -configuration Debug build
xcrun simctl install "$SIM_ID" ~/Library/Developer/Xcode/DerivedData/iosApp-emryfhatixepiidrvymvyhzquzdk/Build/Products/Debug-iphonesimulator/iosApp.app
xcrun simctl launch "$SIM_ID" com.blueedge.chainreaction