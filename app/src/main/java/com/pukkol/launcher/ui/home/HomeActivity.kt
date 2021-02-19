package com.pukkol.launcher.ui.home

import android.Manifest.permission.INSTALL_SHORTCUT
import android.app.Activity
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.pukkol.launcher.R
import com.pukkol.launcher.Setup
import com.pukkol.launcher.data.model.App
import com.pukkol.launcher.interfaces.AppDeleteListener
import com.pukkol.launcher.interfaces.AppUpdateListener
import com.pukkol.launcher.interfaces.IWallpaperListener
import com.pukkol.launcher.services.AppUpdateReceiver
import com.pukkol.launcher.services.ShortcutReceiver
import com.pukkol.launcher.ui.home.desktop.DesktopFragment
import com.pukkol.launcher.ui.home.menu.MenuFragment
import com.pukkol.launcher.util.Display
import kotlinx.android.synthetic.main.activity_home.*
import java.io.IOException


@Suppress("DEPRECATION")// get wallpaper image
class HomeActivity : FragmentActivity(), IWallpaperListener {
    private lateinit var touchView: HomeTouchView
    private lateinit var menuFragment: MenuFragment
    private lateinit var desktopFragment: DesktopFragment
    private lateinit var fragmentSwitcher: FragmentSwitcher
    private var appUpdateReceiver: AppUpdateReceiver? = null
    private var shortcutReceiver: ShortcutReceiver? = null

    companion object {
        val tag = HomeActivity::class.simpleName

        // finals
        const val PICK_IMAGE = 1
        const val PICK_APPWIDGET = 9848

        // models
        var launcher: HomeActivity? = null // todo try to remove
        private val sAppUpdateIntentFilter = IntentFilter()
        private val sShortcutIntentFilter = IntentFilter()

        init {
            sAppUpdateIntentFilter.addDataScheme("package")
            sAppUpdateIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
            sAppUpdateIntentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
            sAppUpdateIntentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED)
            sShortcutIntentFilter.addAction(INSTALL_SHORTCUT)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        touchView = item_option_view!!
        menuFragment = MenuFragment(touchView)
        desktopFragment = DesktopFragment(this, touchView)
        fragmentSwitcher = FragmentSwitcher(menuFragment, desktopFragment)

        // fragment manager
        supportFragmentManager.beginTransaction()
            .add(R.id.container, desktopFragment, DesktopFragment::class.java.simpleName)
            .add(R.id.container, menuFragment, MenuFragment::class.java.simpleName)
            .commit()

        // update listeners
        Setup.appManager().addUpdateListener(
                object : AppUpdateListener {
                    override fun onAppUpdated(apps: ArrayList<App>): Boolean {
                        desktopFragment.init()
                        return false// keep
                    }
                }
        )
        Setup.appManager().addDeleteListener(
                object : AppDeleteListener {
                    override fun onAppsDeleted(apps: ArrayList<App>): Boolean {
                        desktopFragment.init()
                        return true // delete
                    }
                }
        )

        // full window UI
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.window.setDecorFitsSystemWindows(false)
        } else {
            this.window.setFlags(FLAG_LAYOUT_NO_LIMITS, FLAG_LAYOUT_NO_LIMITS)
        }

        registerBroadcastReceiver()
    }

    override fun onResume() {
        launcher = this
        super.onResume()

        desktopFragment.handleLauncherResume()
        touchView!!.dragHelper!!.onBackPressed()
    }

    override fun onStart() {
        launcher = this
        super.onStart()
    }

    override fun onDestroy() {
        menuFragment.onDestroy()
        desktopFragment.onDestroy()
        fragmentSwitcher.onDestroy()
        unRegisterBroadcastReceiver()
        super.onDestroy()
    }

    override fun onBackPressed() {
        fragmentSwitcher.onBackPressed()
        touchView!!.dragHelper!!.onBackPressed()
    }

    override fun onWallpaperPick() {
        val pickIntent = Intent()
                .setAction(Intent.ACTION_GET_CONTENT)
                .setType("image/*")

        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            onActivityResult(PICK_IMAGE, result)
        }.launch(pickIntent)
    }

    override fun onWallpaperChanged(bitmap: Bitmap, onHomeScreen: Boolean, onLockScreen: Boolean) {
        val wallpaperManager = WallpaperManager.getInstance(applicationContext)
        if (onHomeScreen) {
            wallpaperManager.setBitmap(bitmap)
        }
        if (onLockScreen) {
            wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK) //For Lock screen
        }

        // calculate average wallpaper color and save it
        Thread {
            Display.AverageColor.setDisplay(this.applicationContext, bitmap)
            Display.AverageColor.setSearchBar(this.applicationContext, bitmap)
            Display.AverageColor.setPageIndicator(this.applicationContext, bitmap)
            Display.AverageColor.setDesktopOptions(this.applicationContext, bitmap)

            // update color on UI
            desktopFragment.onEditWallpaper()
            menuFragment.searchLayout!!.updateContentColor()
        }.start()

        onResume()
    }

    fun onActivityResult(request: Int, result: ActivityResult) {
        val data = result.data
        val resultCode = result.resultCode

        if(result.resultCode == Activity.RESULT_OK) {
            if (request == PICK_IMAGE) {
                // wallpaper
                try {
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(baseContext.contentResolver, data!!.data!!))
                    } else {
                        MediaStore.Images.Media.getBitmap(baseContext.contentResolver, data?.data)
                    }

                    // ignoreResume = true;
                    desktopFragment.wallpaperOptionView!!.prepareWallpaper(bitmap)
                } catch (e: IOException) {
                    Log.i(tag, "Some exception $e")
                }
                return
            }
        }

        // widget
        val extra = data?.extras ?: return
        val appWidgetID = extra.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        desktopFragment.setWidgetItem(request, resultCode, appWidgetID)

    }

    private fun registerBroadcastReceiver() {
        appUpdateReceiver = AppUpdateReceiver()
        shortcutReceiver = ShortcutReceiver(desktopFragment)

        // register all receivers
        this.registerReceiver(appUpdateReceiver, sAppUpdateIntentFilter)
        this.registerReceiver(shortcutReceiver, sShortcutIntentFilter)
    }

    private fun unRegisterBroadcastReceiver() {
        // unregister all receivers
        unregisterReceiver(appUpdateReceiver)
        unregisterReceiver(shortcutReceiver)
    }

}