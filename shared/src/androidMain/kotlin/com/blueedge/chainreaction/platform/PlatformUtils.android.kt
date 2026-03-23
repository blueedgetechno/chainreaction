package com.blueedge.chainreaction.platform

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun openUrl(url: String) {
    // Handled by the Activity — this is a fallback that does nothing.
    // The Android app overrides URL opening via the CompositionLocal or Activity context.
}
