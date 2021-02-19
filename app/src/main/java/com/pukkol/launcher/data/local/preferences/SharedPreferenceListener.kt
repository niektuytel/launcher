package com.pukkol.launcher.data.local.preferences

interface SharedPreferenceListener<TKEY, TVALUE> {
    fun getString(key: TKEY, defaultValue: String?): String?
    fun getInt(key: TKEY, defaultValue: Int): Int
    fun getLong(key: TKEY, defaultValue: Long): Long
    fun getBool(key: TKEY, defaultValue: Boolean): Boolean
    fun getFloat(key: TKEY, defaultValue: Float): Float
    fun getDouble(key: TKEY, defaultValue: Double): Double
    fun getIntList(key: TKEY): List<Int>
    fun getStringList(key: TKEY): List<String>
    fun setString(key: TKEY, value: String?): TVALUE
    fun setInt(key: TKEY, value: Int): TVALUE
    fun setLong(key: TKEY, value: Long): TVALUE
    fun setBool(key: TKEY, value: Boolean): TVALUE
    fun setFloat(key: TKEY, value: Float): TVALUE
    fun setDouble(key: TKEY, value: Double): TVALUE
    fun setIntList(key: TKEY, value: List<Int>): TVALUE
    fun setStringList(key: TKEY, value: List<String>): TVALUE
}