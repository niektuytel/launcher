package com.pukkol.launcher.data.local.preferences

import com.pukkol.launcher.R
import com.pukkol.launcher.util.Definitions.WallpaperScroll

/**
 * get/set data from shared preferences storage
 * @Author Niek Tuytel (Okido)
 */
class SPDesktopHelper(helper: SharedPreferencesHelper) : SharedPreferencesHelper(helper.context, helper.defaultPreferencesName) {
    val currentPage: Int
        get() = getInt(R.string.pref_key__desktop_current_position, 0)
    val wallpaperScroll: WallpaperScroll
        get() {
            val value = getIntOfString(R.string.pref_key__desktop_wallpaper_scroll, 0)
            return when (value) {
                0 -> WallpaperScroll.NORMAL
                1 -> WallpaperScroll.INVERSE
                2 -> WallpaperScroll.OFF
                else -> WallpaperScroll.NORMAL
            }
        }
}