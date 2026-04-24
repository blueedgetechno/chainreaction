package com.blueedge.chainreaction.platform

import android.content.Context

/** Holds the Application context so platform utilities can use it without capturing an Activity. */
internal object AndroidContextHolder {
    lateinit var appContext: Context
}
