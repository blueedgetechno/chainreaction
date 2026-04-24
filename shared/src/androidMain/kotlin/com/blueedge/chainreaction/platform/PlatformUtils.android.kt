package com.blueedge.chainreaction.platform

import android.content.Intent
import android.net.Uri

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun openUrl(url: String) {
    val context = AndroidContextHolder.appContext
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
