package com.pukkol.launcher.data.local

import android.content.Context
import com.pukkol.launcher.R
import com.pukkol.launcher.data.local.preferences.SPDesktopHelper
import com.pukkol.launcher.data.local.preferences.SPDeviceHelper
import com.pukkol.launcher.data.local.preferences.SharedPreferencesHelper

/**
 * DeviceSettings is capable of handle all data of the `shared preferences` data
 * he gets and set all settings data that is required
 * @Author Niek Tuytel (Okido)
 */
class DeviceSettings(context: Context?) : SharedPreferencesHelper(context, "app") {
    val language: String?
        get() = getString(R.string.pref_key__language, "")
    val animationSpeed: Int
        get() = 100 - getInt(R.string.pref_key__animation_speed, 80)
    var introLaunch: Boolean
        get() = getBool(R.string.pref_key__device_visibility_state_intro, true)
        set(visible) {
            setBool(R.string.pref_key__device_visibility_state_intro, visible)
        }
    var remotelyControlled: Boolean
        get() = getBool(R.string.pref_key__device_remotely_controlled, false)
        set(value) {
            setBool(R.string.pref_key__device_remotely_controlled, value)
        }

    var badgeNotification: Boolean
        get() = getBool(R.string.pref_key__device_badge_notifications, false)
        set(access) {
            setBool(R.string.pref_key__device_badge_notifications, access)
        }
    var cellHorizontalAmount: Int
        get() = getInt(R.string.pref_key__device_amount_cells_horizontal, 5)
        set(amount) {
            setInt(R.string.pref_key__device_amount_cells_horizontal, amount)
        }
    var cellWidth: Int
        get() = getInt(R.string.pref_key__device_cell_width, 50)
        set(width) {
            setInt(R.string.pref_key__device_cell_width, width)
        }

    fun device(): SPDeviceHelper {
        return SPDeviceHelper(this)
    }

    fun desktop(): SPDesktopHelper {
        return SPDesktopHelper(this)
    }
}


//    public void setRemovedApps(ArrayList<String> apps) { setStringList(R.string.pref_key__device_removed_apps, apps); }
//    public List<String> getRemovedApps() { return getStringList(R.string.pref_key__device_removed_apps); }
//    public void setLimitedApps(ArrayList<String> apps) { setStringList(R.string.pref_key__device_removed_apps, apps); }
//    public List<String> getLimitedApps() { return getStringList(R.string.pref_key__device_removed_apps); }