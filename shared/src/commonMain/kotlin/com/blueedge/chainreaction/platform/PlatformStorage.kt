package com.blueedge.chainreaction.platform

/** Abstraction for key-value persistence (SharedPreferences on Android, NSUserDefaults on iOS). */
interface PlatformStorage {
    fun getString(key: String, default: String): String
    fun putString(key: String, value: String)
    fun getInt(key: String, default: Int): Int
    fun putInt(key: String, value: Int)
    fun getBoolean(key: String, default: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun apply()
}

/** In-memory fallback storage (used before platform storage is initialized). */
class InMemoryStorage : PlatformStorage {
    private val strings = mutableMapOf<String, String>()
    private val ints = mutableMapOf<String, Int>()
    private val booleans = mutableMapOf<String, Boolean>()

    override fun getString(key: String, default: String) = strings[key] ?: default
    override fun putString(key: String, value: String) { strings[key] = value }
    override fun getInt(key: String, default: Int) = ints[key] ?: default
    override fun putInt(key: String, value: Int) { ints[key] = value }
    override fun getBoolean(key: String, default: Boolean) = booleans[key] ?: default
    override fun putBoolean(key: String, value: Boolean) { booleans[key] = value }
    override fun apply() {}
}
