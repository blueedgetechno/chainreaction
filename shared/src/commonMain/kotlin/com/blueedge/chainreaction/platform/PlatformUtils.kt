package com.blueedge.chainreaction.platform

/** Platform-specific utility functions. */
expect fun currentTimeMillis(): Long

/** Open a URL in the platform's default browser. */
expect fun openUrl(url: String)
