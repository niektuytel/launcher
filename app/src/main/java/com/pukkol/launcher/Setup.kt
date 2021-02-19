package com.pukkol.launcher

import com.pukkol.launcher.data.local.DeviceSettings
import com.pukkol.launcher.data.local.db.DBItemHelper
import com.pukkol.launcher.data.model.AppManager

/**
 * @author okido (Niek Tuytel)[2/9/2021]
 * Setup is a Extention of InitSetup
 * here it is possible to use the data from InitSetup
 */
abstract class Setup {
    abstract val deviceSettings: DeviceSettings
    abstract val itemManager: DBItemHelper
    abstract val appManager: AppManager

    companion object {
        private var sInstance: Setup? = null
        val isInitialised: Boolean
            get() = sInstance != null

        fun get(): Setup? {
            if (sInstance == null) {
                throw RuntimeException("Setup has not been initialised!")
            }
            return sInstance
        }

        fun deviceSettings(): DeviceSettings {
            return get()!!.deviceSettings
        }

        fun itemManager(): DBItemHelper {
            return get()!!.itemManager
        }

        fun appManager(): AppManager {
            return get()!!.appManager
        }
    }

    init {
        sInstance = this
    }
}