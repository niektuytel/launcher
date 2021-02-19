package com.pukkol.launcher

import com.pukkol.launcher.data.local.DeviceSettings
import com.pukkol.launcher.data.local.db.DBItemHelper
import com.pukkol.launcher.data.model.AppManager

/**
 * @author okido (Niek Tuytel)[2/9/2021]
 * Init setup only call when you want to recreate the Setup
 * this only happens the first usage or when there accourd a lot of bugs
 * that we refresh the application for the user.
 */
class SetupInit(activity: MainActivity?) : Setup() {
    override val appManager: AppManager = AppManager(activity!!)
    override val itemManager: DBItemHelper = DBItemHelper(activity)
    override val deviceSettings: DeviceSettings = DeviceSettings(activity)
}