package com.pukkol.launcher.interfaces

import com.pukkol.launcher.data.model.App

interface AppUpdateListener {
    fun onAppUpdated(apps: ArrayList<App>): Boolean
}