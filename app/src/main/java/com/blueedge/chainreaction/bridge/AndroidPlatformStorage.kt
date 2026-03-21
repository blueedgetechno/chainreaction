package com.blueedge.chainreaction.bridge

import android.content.Context
import com.blueedge.chainreaction.platform.PlatformStorage

/** Bridges Android SharedPreferences to the shared PlatformStorage interface. */
class AndroidPlatformStorage(context: Context) : PlatformStorage {
    private val prefs = context.getSharedPreferences("chain_reaction_prefs", Context.MODE_PRIVATE)

    override fun getString(key: String, default: String): String = prefs.getString(key, default) ?: default
    override fun putString(key: String, value: String) { prefs.edit().putString(key, value).apply() }
    override fun getInt(key: String, default: Int): Int = prefs.getInt(key, default)
    override fun putInt(key: String, value: Int) { prefs.edit().putInt(key, value).apply() }
    override fun getBoolean(key: String, default: Boolean): Boolean = prefs.getBoolean(key, default)
    override fun putBoolean(key: String, value: Boolean) { prefs.edit().putBoolean(key, value).apply() }
    override fun apply() { /* no-op, each put already applies */ }
}
