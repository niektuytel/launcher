package com.pukkol.launcher.data.model

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.util.Log
import com.pukkol.launcher.interfaces.AppDeleteListener
import com.pukkol.launcher.interfaces.AppUpdateListener
import java.text.Collator
import java.util.*
import kotlin.collections.ArrayList

class AppManager(private val mContext: Context) {
    private val mPackageManager: PackageManager
    val updateListeners: MutableList<AppUpdateListener> = ArrayList()
    val deleteListeners: MutableList<AppDeleteListener> = ArrayList()
    private var mApps: ArrayList<App> = ArrayList()
    private var mNonFilteredApps: ArrayList<App> = ArrayList()

    // update apps on Broadcast listener
    fun onAppUpdated(context: Context?, intent: Intent?) {
        createApps()
        notifyUpdateListeners()
    }

    fun addUpdateListener(updateListener: AppUpdateListener) {
        updateListeners.add(updateListener)
    }

    fun addDeleteListener(deleteListener: AppDeleteListener) {
        deleteListeners.add(deleteListener)
    }

    fun notifyUpdateListeners() {
        val iterator = updateListeners.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().onAppUpdated(mNonFilteredApps)) {
                iterator.remove()
            }
        }
    }

    fun notifyUpdateListeners(apps: ArrayList<App>) {
        val iterator = updateListeners.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().onAppUpdated(apps)) {
                iterator.remove()
            }
        }
    }

    fun notifyRemoveListeners(apps: ArrayList<App>) {
        val iterator = deleteListeners.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().onAppsDeleted(apps)) {
                iterator.remove()
            }
        }
    }

    fun findApp(intent: Intent?): App? {
        if (intent == null || intent.component == null) return null
        val packageName = intent.component!!.packageName
        val className = intent.component!!.className
        for (app in mApps) {
            if (app.className == className && app.packageName == packageName) {
                return app
            }
        }
        return null
    }

    fun getApps(includedSystemApps: Boolean): ArrayList<App> {
        return if (includedSystemApps) {
            mNonFilteredApps
        } else {
            mApps
        }
    }

    fun createApp(intent: Intent?): App? {
        return try {
            val info = mPackageManager.resolveActivity(intent!!, 0)!!
            App(mPackageManager, info)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createApps() {
        mApps = ArrayList()
        mNonFilteredApps = ArrayList()

        // work profile support
        val launcherApps = mContext.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val profiles = launcherApps.profiles
        for (userHandle in profiles) {
            val apps = launcherApps.getActivityList(null, userHandle)
            for (info in apps) {
                val app = App(mPackageManager, info)
                app.userHandle = userHandle
                Log.d(TAG, "APP: " + app.label + ", " + app.packageName + ", " + app.className)
                mApps.add(app)
            }
        }

        // sort the apps by label here
        Collections.sort(
                mApps
        ) { one: App, two: App -> Collator.getInstance().compare(one.label, two.label) }
        mNonFilteredApps = removeSystemApps(mApps)
    }

    private fun removeSystemApps(apps: List<App>): ArrayList<App> {
        val filteredApps = ArrayList<App>()
        val unwantedApps = arrayOf(
                "com.pukkol.launcher",
                "com.pukkol.launcher.debug",
                "com.pukkol.launcher.release"
        )
        for (app in apps) {
            var ignore = false
            for (packageName in unwantedApps) {
                if (app.packageName == packageName) {
                    ignore = true
                    break
                }
            }
            if (!ignore) {
                filteredApps.add(app)
            }
        }
        return filteredApps
    }

    companion object {
        private val TAG = AppManager::class.java.simpleName
    }

    init {
        mPackageManager = mContext.packageManager
        createApps()
    }
}