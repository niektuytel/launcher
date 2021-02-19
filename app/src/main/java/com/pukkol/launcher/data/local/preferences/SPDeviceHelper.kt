package com.pukkol.launcher.data.local.preferences

import android.graphics.Color
import com.pukkol.launcher.R

class SPDeviceHelper(prefHelper: SharedPreferencesHelper) : SharedPreferencesHelper(prefHelper.context, prefHelper.defaultPreferencesName) {
    // below not seen usage
    val wallpaperColor: Color
        get() {
            val color = getInt(R.string.pref_key__device_average_wallpaper_color, Color.BLACK)
            return Color.valueOf(color)
        }
    val areaSearchBoxColor: Color
        get() {
            val color = getInt(R.string.pref_key__device_average_area_searchBox_color, Color.BLACK)
            return Color.valueOf(color)
        }
    val areaPageIndicatorColor: Color
        get() {
            val color = getInt(R.string.pref_key__device_average_area_pageIndicator_color, Color.BLACK)
            return Color.valueOf(color)
        }
    val areaDesktopOptionsColor: Color
        get() {
            val color = getInt(R.string.pref_key__device_average_area_desktopOptions_color, Color.BLACK)
            return Color.valueOf(color)
        }

    fun setWallpaperColor(color: Int) {
        setInt(R.string.pref_key__device_average_wallpaper_color, color)
    }

    fun setAreaSearchBoxColor(color: Int) {
        setInt(R.string.pref_key__device_average_area_searchBox_color, color)
    }

    fun setAreaPageIndicatorColor(color: Int) {
        setInt(R.string.pref_key__device_average_area_pageIndicator_color, color)
    }

    fun setAreaDesktopOptionsColor(color: Int) {
        setInt(R.string.pref_key__device_average_area_desktopOptions_color, color)
    }
}