package com.pukkol.app_settings

import android.app.Activity
import android.content.Context
import android.content.pm.LauncherApps
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View.GONE
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pukkol.app_settings.SettingsActivity.Companion.dataRemoveKey
import kotlinx.android.synthetic.main.activity_remove_apps.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper

/**
 * `RemovedPackages` is the key of the removed applications
 * and as well return on this key with the new data
 **/
class RemoveAppsActivity : AppCompatActivity() {
    private lateinit var mAppList : RecyclerView
    private var mAdapter = AppAdapter(this)

    private var mRemovedApps = ArrayList<String>()
    private var mApps = ArrayList<App>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remove_apps)
        setSupportActionBar(toolbar_remove_apps)

        // applications
        mAppList = recycler_view_remove_apps
        mAppList.layoutManager = LinearLayoutManager(this)
        mAppList.adapter = mAdapter
        OverScrollDecoratorHelper.setUpOverScroll(mAppList, OverScrollDecoratorHelper.ORIENTATION_VERTICAL)

        // get intents
        try {
            mRemovedApps = this.intent.getStringArrayListExtra(dataRemoveKey()) as ArrayList<String>
        } catch (exception : TypeCastException) {
            Log.d("RemoveAppsActivity", "Intent data is empty please add a Intent with the key: `${dataRemoveKey()}` of `ArrayList<String>` with Removed app package names")
            exception.printStackTrace()
        }

        // progress
        val progress = progressBar
        GlobalScope.launch {
            setInstalledApps()

            runOnUiThread {
                Runnable { progress.visibility = GONE }
            }
        }
    }

    override fun onStop() {
        setIntentResult()
        super.onStop()
    }

    // enable the back function to the button on press
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        mRemovedApps.addAll(mAdapter.getRemovedItems())

        // update data
        setIntentResult()
        super.onBackPressed()
    }

    private fun setInstalledApps() {
        // work profile support
        val launcherApps = baseContext.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val profiles = launcherApps.profiles
        for (userHandle in profiles) {
            val apps = launcherApps.getActivityList(null, userHandle)
            for (info in apps) {
                addApp(App(info))
            }
        }
    }

    private fun addApp(app : App) {
        if(!mRemovedApps.contains(app.packageName)) {
            Log.d("RemoveAppsActivity::addApp()", "APP: " + app.label + ", " + app.packageName)
            mApps.add(app)

            runOnUiThread(
                    Runnable { mAdapter.addItem(app); }
            )
        }
    }

    private fun setIntentResult() {
        intent.putExtra(dataRemoveKey(), mRemovedApps)
        setResult(Activity.RESULT_OK, intent)
    }
}

