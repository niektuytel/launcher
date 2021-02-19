package com.pukkol.launcher.data.local.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.text.TextUtils
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import java.util.*

/**
 * Wrapper for settings based on SharedPreferences, optionally with keys in resources
 * Default Shared Preference (mPrefApp) will be taken if no SP is specified, else the first one
 */
open class SharedPreferencesHelper @JvmOverloads constructor(
        /**
         * Methods
         */
        val context: Context?, prefAppName: String? = SHARED_PREF_APP) : SharedPreferenceListener<String, SharedPreferencesHelper>
{
    private val defaultPreferences: SharedPreferences
    val defaultPreferencesName: String = if (TextUtils.isEmpty(prefAppName)) context!!.packageName + "_preferences" else prefAppName!!
    fun isKeyEqual(key: String, keyResourceId: Int): Boolean {
            return key == stringFromResource(keyResourceId)
    }

    @JvmOverloads
    fun resetSettings(pref: SharedPreferences = defaultPreferences) {
        pref.edit().clear().apply()
    }

    fun isPrefSet(@StringRes keyResourceId: Int): Boolean {
        return isPrefSet(defaultPreferences, keyResourceId)
    }

    private fun isPrefSet(pref: SharedPreferences, @StringRes keyResourceId: Int): Boolean {
        return pref.contains(stringFromResource(keyResourceId))
    }

    fun registerPreferencesChangedListener(changeListener: OnSharedPreferenceChangeListener?) {
        registerPreferencesChangedListener(defaultPreferences, changeListener)
    }

    fun registerPreferencesChangedListener(pref: SharedPreferences, changeListener: OnSharedPreferenceChangeListener?) {
        pref.registerOnSharedPreferenceChangeListener(changeListener)
    }

    fun unregisterPreferenceChangedListener(changeListener: OnSharedPreferenceChangeListener?) {
        unregisterPreferenceChangedListener(defaultPreferences, changeListener)
    }

    fun unregisterPreferenceChangedListener(pref: SharedPreferences, changeListener: OnSharedPreferenceChangeListener?) {
        pref.unregisterOnSharedPreferenceChangeListener(changeListener)
    }

    val defaultPreferencesEditor: Editor
        get() = defaultPreferences.edit()

    private fun getFirstPreference(vararg prefs: SharedPreferences): SharedPreferences {
        return if (prefs.isNotEmpty()) prefs[0] else defaultPreferences
    }

    /**
     * Getter for resources
     */
    private fun stringFromResource(@StringRes keyResourceId: Int): String {
        return context!!.getString(keyResourceId)
    }

    private fun colorFromResource(@ColorRes keyResourceId: Int): Int {
        return ContextCompat.getColor(context!!, keyResourceId)
    }

    fun stringsFromResources(vararg keyResourceIds: Int): Array<String?> {
        val ret = arrayOfNulls<String>(keyResourceIds.size)
        for (i in ret.indices) {
            ret[i] = stringFromResource(keyResourceIds[i])
        }
        return ret
    }

    /**
     * Getter & Setter for String + String Array
     */
    fun setString(key: String?, value: String?, vararg prefs: SharedPreferences) {
        getFirstPreference(*prefs).edit().putString(key, value).apply()
    }

    fun setString(@StringRes keyResourceId: Int, value: String?, vararg prefs: SharedPreferences) {
        setString(stringFromResource(keyResourceId), value, *prefs)
    }

    fun setString(key: String?, @StringRes valueResourceId: Int, vararg prefs: SharedPreferences) {
        setString(key, stringFromResource(valueResourceId), *prefs)
    }

    fun setString(@StringRes keyResourceId: Int, @StringRes valueResourceId: Int, vararg prefs: SharedPreferences) {
        setString(stringFromResource(keyResourceId), stringFromResource(valueResourceId), *prefs)
    }

    fun getString(key: String?, defaultValue: String?, vararg prefs: SharedPreferences): String? {
        return getFirstPreference(*prefs).getString(key, defaultValue)
    }

    fun getString(@StringRes keyResourceId: Int, defaultValue: String?, vararg prefs: SharedPreferences): String? {
        return getString(stringFromResource(keyResourceId), defaultValue, *prefs)
    }

    fun getString(key: String?, @StringRes defaultValueResourceId: Int, vararg prefs: SharedPreferences): String? {
        return getString(key, stringFromResource(defaultValueResourceId), *prefs)
    }

    fun getString(@StringRes keyResourceId: Int, @StringRes defaultValueResourceId: Int, vararg prefs: SharedPreferences): String? {
        return getString(stringFromResource(keyResourceId), stringFromResource(defaultValueResourceId), *prefs)
    }

    private fun setStringListPref(key: String, values: List<String>, pref: SharedPreferences) {
        val sb = StringBuilder()
        for (value in values) {
            sb.append(ARRAY_SEPARATOR)
            sb.append(value.replace(ARRAY_SEPARATOR, ARRAY_SEPARATOR_SUBSTITUTE))
        }
        setString(key, sb.toString().replaceFirst(ARRAY_SEPARATOR.toRegex(), ""), pref)
    }

    private fun getStringListPref(key: String?, pref: SharedPreferences): ArrayList<String> {
        val ret = ArrayList<String>()
        val value = pref.getString(key, ARRAY_SEPARATOR)!!.replace(ARRAY_SEPARATOR_SUBSTITUTE, ARRAY_SEPARATOR)
        if (value == ARRAY_SEPARATOR || TextUtils.isEmpty(value)) {
            return ret
        }
        ret.addAll(Arrays.asList(*value.split(ARRAY_SEPARATOR.toRegex()).toTypedArray()))
        return ret
    }

    fun setStringArray(key: String, values: Array<String?>, vararg prefs: SharedPreferences) {
        setStringListPref(key, Arrays.asList<String>(*values), getFirstPreference(*prefs))
    }

    fun setStringArray(@StringRes keyResourceId: Int, values: Array<String?>, vararg prefs: SharedPreferences) {
        setStringArray(stringFromResource(keyResourceId), values, *prefs)
    }

    fun setStringList(@StringRes keyResourceId: Int, values: List<String?>, vararg pref: SharedPreferences) {
        setStringArray(stringFromResource(keyResourceId), values.toTypedArray(), *pref)
    }

    fun setStringList(key: String, values: List<String?>, vararg pref: SharedPreferences) {
        setStringArray(key, values.toTypedArray(), *pref)
    }

    fun getStringArray(key: String?, vararg prefs: SharedPreferences): Array<String> {
        val list: List<String> = getStringList(key, getFirstPreference(*prefs))
        return list.toTypedArray()
    }

    fun getStringArray(@StringRes keyResourceId: Int, vararg prefs: SharedPreferences): Array<String> {
        return getStringArray(stringFromResource(keyResourceId), *prefs)
    }

    fun getStringList(@StringRes keyResourceId: Int, vararg prefs: SharedPreferences): ArrayList<String> {
        return getStringList(stringFromResource(keyResourceId), getFirstPreference(*prefs))
    }

    fun getStringList(key: String?, vararg prefs: SharedPreferences): ArrayList<String> {
        return getStringListPref(key, getFirstPreference(*prefs))
    }

    /**
     * Getter & Setter for integer
     */
    fun setInt(key: String?, value: Int, vararg prefs: SharedPreferences) {
        getFirstPreference(*prefs).edit().putInt(key, value).apply()
    }

    fun setInt(@StringRes keyResourceId: Int, value: Int, vararg prefs: SharedPreferences) {
        setInt(stringFromResource(keyResourceId), value, *prefs)
    }

    fun getInt(key: String?, defaultValue: Int, vararg prefs: SharedPreferences): Int {
        return getFirstPreference(*prefs).getInt(key, defaultValue)
    }

    fun getInt(@StringRes keyResourceId: Int, defaultValue: Int, vararg prefs: SharedPreferences): Int {
        return getInt(stringFromResource(keyResourceId), defaultValue, *prefs)
    }

    fun getIntOfString(key: String?, defaultValue: Int, vararg prefs: SharedPreferences): Int {
        val strNum = getString(key, Integer.toString(defaultValue), getFirstPreference(*prefs))
        return strNum!!.toInt()
    }

    fun getIntOfString(@StringRes keyResId: Int, defaultValue: Int, vararg prefs: SharedPreferences): Int {
        return getIntOfString(stringFromResource(keyResId), defaultValue, *prefs)
    }

    private fun setIntListPref(key: String, values: List<Int>, pref: SharedPreferences) {
        val sb = StringBuilder()
        for (value in values) {
            sb.append(ARRAY_SEPARATOR)
            sb.append(value.toString())
        }
        setString(key, sb.toString().replaceFirst(ARRAY_SEPARATOR.toRegex(), ""), pref)
    }

    private fun getIntListPref(key: String, pref: SharedPreferences): ArrayList<Int> {
        val ret = ArrayList<Int>()
        val value = pref.getString(key, ARRAY_SEPARATOR)
        if (value == ARRAY_SEPARATOR) {
            return ret
        }
        for (s in value!!.split(ARRAY_SEPARATOR.toRegex()).toTypedArray()) {
            ret.add(s.toInt())
        }
        return ret
    }

    fun setIntArray(key: String, values: Array<Int?>, vararg prefs: SharedPreferences) {
        setIntListPref(key, Arrays.asList<Int>(*values), getFirstPreference(*prefs))
    }

    fun setIntArray(@StringRes keyResourceId: Int, values: Array<Int?>, vararg prefs: SharedPreferences) {
        setIntArray(stringFromResource(keyResourceId), values, getFirstPreference(*prefs))
    }

    fun getIntArray(key: String, vararg prefs: SharedPreferences): Array<Int> {
        val data: List<Int> = getIntListPref(key, getFirstPreference(*prefs))
        return data.toTypedArray()
    }

    fun getIntArray(@StringRes keyResourceId: Int, vararg prefs: SharedPreferences): Array<Int> {
        return getIntArray(stringFromResource(keyResourceId), getFirstPreference(*prefs))
    }

    fun setIntList(key: String, values: List<Int>, vararg prefs: SharedPreferences) {
        setIntListPref(key, values, getFirstPreference(*prefs))
    }

    fun setIntList(@StringRes keyResourceId: Int, values: List<Int>, vararg prefs: SharedPreferences) {
        setIntList(stringFromResource(keyResourceId), values, *prefs)
    }

    fun getIntList(key: String, vararg prefs: SharedPreferences): ArrayList<Int> {
        return getIntListPref(key, getFirstPreference(*prefs))
    }

    fun getIntList(@StringRes keyResourceId: Int, vararg prefs: SharedPreferences): ArrayList<Int> {
        return getIntList(stringFromResource(keyResourceId), *prefs)
    }

    /**
     * Getter & Setter for Long
     */
    fun setLong(key: String?, value: Long, vararg prefs: SharedPreferences) {
        getFirstPreference(*prefs).edit().putLong(key, value).apply()
    }

    fun setLong(@StringRes keyResourceId: Int, value: Long, vararg prefs: SharedPreferences) {
        setLong(stringFromResource(keyResourceId), value, *prefs)
    }

    fun getLong(key: String?, defaultValue: Long, vararg prefs: SharedPreferences): Long {
        return getFirstPreference(*prefs).getLong(key, defaultValue)
    }

    fun getLong(@StringRes keyResourceId: Int, defaultValue: Long, vararg prefs: SharedPreferences): Long {
        return getLong(stringFromResource(keyResourceId), defaultValue, *prefs)
    }

    /**
     * Getter & Setter for Float
     */
    fun setFloat(key: String?, value: Float, vararg prefs: SharedPreferences) {
        getFirstPreference(*prefs).edit().putFloat(key, value).apply()
    }

    fun setFloat(@StringRes keyResourceId: Int, value: Float, vararg prefs: SharedPreferences) {
        setFloat(stringFromResource(keyResourceId), value, *prefs)
    }

    fun getFloat(key: String?, defaultValue: Float, vararg prefs: SharedPreferences): Float {
        return getFirstPreference(*prefs).getFloat(key, defaultValue)
    }

    fun getFloat(@StringRes keyResourceId: Int, defaultValue: Float, vararg prefs: SharedPreferences): Float {
        return getFloat(stringFromResource(keyResourceId), defaultValue, *prefs)
    }

    /**
     * Getter & Setter for Double
     */
    fun setDouble(key: String?, value: Double, vararg prefs: SharedPreferences) {
        setLong(key, java.lang.Double.doubleToRawLongBits(value), *prefs)
    }

    fun setDouble(@StringRes keyResourceId: Int, value: Double, vararg prefs: SharedPreferences) {
        setDouble(stringFromResource(keyResourceId), java.lang.Double.doubleToRawLongBits(value).toDouble(), *prefs)
    }

    fun getDouble(key: String?, defaultValue: Double, vararg prefs: SharedPreferences): Double {
        return java.lang.Double.longBitsToDouble(getLong(key, java.lang.Double.doubleToRawLongBits(defaultValue), getFirstPreference(*prefs)))
    }

    fun getDouble(@StringRes keyResourceId: Int, defaultValue: Double, vararg prefs: SharedPreferences): Double {
        return getDouble(stringFromResource(keyResourceId), defaultValue, *prefs)
    }

    /**
     * Getter & Setter for Boolean
     */
    @SuppressLint("ApplySharedPref")
    fun setBoolCommit(key: String?, value: Boolean, vararg prefs: SharedPreferences) {
        getFirstPreference(*prefs).edit().putBoolean(key, value).commit()
    }

    fun setBoolCommit(@StringRes keyResourceId: Int, value: Boolean, vararg prefs: SharedPreferences) {
        setBoolCommit(stringFromResource(keyResourceId), value, *prefs)
    }

    fun setBool(key: String?, value: Boolean, vararg prefs: SharedPreferences) {
        getFirstPreference(*prefs).edit().putBoolean(key, value).apply()
    }

    fun setBool(@StringRes keyResourceId: Int, value: Boolean, vararg prefs: SharedPreferences) {
        setBool(stringFromResource(keyResourceId), value, *prefs)
    }

    fun getBool(key: String?, defaultValue: Boolean, vararg prefs: SharedPreferences): Boolean {
        return getFirstPreference(*prefs).getBoolean(key, defaultValue)
    }

    fun getBool(@StringRes keyResourceId: Int, defaultValue: Boolean, vararg prefs: SharedPreferences): Boolean {
        return getBool(stringFromResource(keyResourceId), defaultValue, *prefs)
    }

    /**
     * Getter & Setter for Color
     */
    fun getColor(key: String?, @ColorRes defaultColor: Int, vararg prefs: SharedPreferences): Int {
        return getFirstPreference(*prefs).getInt(key, colorFromResource(defaultColor))
    }

    fun getColor(@StringRes keyResourceId: Int, @ColorRes defaultColor: Int, vararg prefs: SharedPreferences): Int {
        return getColor(stringFromResource(keyResourceId), defaultColor, *prefs)
    }

    /**
     * SharedPreferenceHelper<String> implementations
    </String> */
    override fun getString(key: String, defaultValue: String?): String? {
        return getString(key, defaultValue, defaultPreferences)
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return getInt(key, defaultValue, defaultPreferences)
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return getLong(key, defaultValue, defaultPreferences)
    }

    override fun getBool(key: String, defaultValue: Boolean): Boolean {
        return getBool(key, defaultValue, defaultPreferences)
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return getFloat(key, defaultValue, defaultPreferences)
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return getDouble(key, defaultValue, defaultPreferences)
    }

    override fun getIntList(key: String): ArrayList<Int> {
        return getIntList(key, defaultPreferences)
    }

    override fun getStringList(key: String): ArrayList<String> {
        return getStringList(key, defaultPreferences)
    }

    override fun setString(key: String, value: String?): SharedPreferencesHelper {
        setString(key, value, defaultPreferences)
        return this
    }

    override fun setInt(key: String, value: Int): SharedPreferencesHelper {
        setInt(key, value, defaultPreferences)
        return this
    }

    override fun setLong(key: String, value: Long): SharedPreferencesHelper {
        setLong(key, value, defaultPreferences)
        return this
    }

    override fun setBool(key: String, value: Boolean): SharedPreferencesHelper {
        setBool(key, value, defaultPreferences)
        return this
    }

    override fun setFloat(key: String, value: Float): SharedPreferencesHelper {
        setFloat(key, value, defaultPreferences)
        return this
    }

    override fun setDouble(key: String, value: Double): SharedPreferencesHelper {
        setDouble(key, value, defaultPreferences)
        return this
    }

    override fun setIntList(key: String, value: List<Int>): SharedPreferencesHelper {
        setIntListPref(key, value, defaultPreferences)
        return this
    }

    override fun setStringList(key: String, value: List<String>): SharedPreferencesHelper {
        setStringListPref(key, value, defaultPreferences)
        return this
    }

    fun contains(key: String?, vararg prefs: SharedPreferences): Boolean {
        return getFirstPreference(*prefs).contains(key)
    }

    /**
     * Substract current datetime by given amount of days
     */
    fun getDateOfDaysAgo(days: Int): Date {
        val cal: Calendar = GregorianCalendar()
        cal.add(Calendar.DATE, -days)
        return cal.time
    }

    /**
     * Substract current datetime by given amount of days and check if the given date passed
     */
    fun didDaysPassedSince(date: Date?, days: Int): Boolean {
        return if (date == null || days < 0) {
            false
        } else date.before(getDateOfDaysAgo(days))
    }

    fun afterDaysTrue(key: String, daysSinceLastTime: Int, firstTime: Int, vararg prefs: SharedPreferences): Boolean {
        var d = Date(System.currentTimeMillis())
        if (!contains(key)) {
            d = getDateOfDaysAgo(daysSinceLastTime - firstTime)
            setLong(key, d.time)
            return firstTime < 1
        } else {
            d = Date(getLong(key, d.time))
        }
        val trigger = didDaysPassedSince(d, daysSinceLastTime)
        if (trigger) {
            setLong(key, Date(System.currentTimeMillis()).time)
        }
        return trigger
    }

    companion object {
        const val SHARED_PREF_APP = "app"
        protected const val ARRAY_SEPARATOR = "%%%"
        protected const val ARRAY_SEPARATOR_SUBSTITUTE = "§§§"
        var debugLog = ""
            private set

//        fun limitListTo(list: List<*>, maxSize: Int, removeDuplicates: Boolean) {
//            var o: Any?
//            var pos: Int
//            var i = 0
//            while (removeDuplicates && i < list.size) {
//                o = list[i]
//                while (list.lastIndexOf(o).also { pos = it } != i && pos >= 0) {
//                    list.removeAt(pos)
//                }
//                i++
//            }
//            while (list.size.also { pos = it } > maxSize && pos > 0) {
//                list.removeAt(list.size - 1)
//            }
//        }

        /**
         * A method to determine if current hour is between begin and end.
         * This is especially useful for time-based light/dark mode
         */
        fun isCurrentHourOfDayBetween(begin: Int, end: Int): Boolean {
            var begin = begin
            var end = end
            begin = if (begin >= 23 || begin < 0) 0 else begin
            end = if (end >= 23 || end < 0) 0 else end
            val h = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
            return h >= begin && h <= end
        }

        fun clearDebugLog() {
            debugLog = ""
        }

        @Synchronized
        fun appendDebugLog(text: String) {
            debugLog += """
                   [${Date()}] $text

                   """.trimIndent()
        }
    }

    /**
     * Members, Constructors
     */
    init {
        defaultPreferences = context!!.getSharedPreferences(defaultPreferencesName, Context.MODE_PRIVATE)
    }
}