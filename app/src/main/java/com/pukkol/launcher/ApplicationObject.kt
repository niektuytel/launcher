package com.pukkol.launcher

import android.app.Application

class ApplicationObject : Application() {
    override fun onCreate() {
        super.onCreate()
        sInstance = this
    }

    companion object {
        private var sInstance: ApplicationObject? = null
        fun get(): ApplicationObject? {
            return sInstance
        }
    }
}