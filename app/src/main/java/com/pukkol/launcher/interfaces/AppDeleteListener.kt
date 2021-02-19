package com.pukkol.launcher.interfaces

import com.pukkol.launcher.data.model.App

interface AppDeleteListener {
    fun onAppsDeleted(apps: ArrayList<App>): Boolean
}