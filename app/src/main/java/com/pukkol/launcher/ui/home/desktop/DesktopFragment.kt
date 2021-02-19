package com.pukkol.launcher.ui.home.desktop

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.ACTION_APPWIDGET_PICK
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.View.inflate
import androidx.activity.result.contract.ActivityResultContracts
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.pukkol.launcher.R
import com.pukkol.launcher.interfaces.IPageListener
import com.pukkol.launcher.interfaces.IWallpaperListener
import com.pukkol.launcher.ui.home.FragmentSwitcher
import com.pukkol.launcher.ui.home.HomeActivity
import com.pukkol.launcher.ui.home.HomeActivity.Companion.PICK_APPWIDGET
import com.pukkol.launcher.ui.home.HomeTouchView
import com.pukkol.launcher.ui.home.desktop.edit.wallpaper.WallpaperOptionView
import com.pukkol.launcher.ui.home.menu.MenuFragment
import com.pukkol.launcher.ui.itemview.ItemViewManager
import com.pukkol.launcher.ui.itemview.ItemWidgetView
import com.pukkol.launcher.ui.itemview.WidgetHost
import com.pukkol.launcher.util.Display
import com.pukkol.launcher.util.Tool
import kotlinx.android.synthetic.main.view_desktop.view.*

class DesktopFragment(homeActivity: HomeActivity, touchView: HomeTouchView?) : Fragment(), IPageListener {
    // ui
    private var drawerLayout: CoordinatorLayout? = null
    var mDesktopView: DesktopLayout? = null
    var wallpaperOptionView: WallpaperOptionView? = null
    var pageSettingsView: PageSettingsView? = null
    var folderView: FolderView? = null

    private var mInstance: CoordinatorLayout? = null
    private val mHomeActivity: HomeActivity
    private var mFragmentSwitcher: FragmentSwitcher? = null
    private val mWallpaperListener: IWallpaperListener
    val touchView: HomeTouchView?
    private var mMenuFragment: MenuFragment? = null
    private var mAlpha = 1.00f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appWidgetManager = AppWidgetManager.getInstance(this.activity)
        appWidgetHost = WidgetHost(this, R.id.app_widget_host)
    }

    override fun onResume() {
        super.onResume()
        if (appWidgetHost != null) {
            appWidgetHost!!.startListening()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.view_desktop, container, false)
        drawerLayout = view.drawer_layout
        mDesktopView = view.desktop_view
        wallpaperOptionView = view.wallpaper_option_view
        pageSettingsView = view.desktop_option_view
        folderView = view.folder_view

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDesktopView!!.onCreateLayout(this)
        // mDesktopView!!.pagerView!!.setPageIndicator(mDesktopView!!.pageIndicatorView)

        mInstance = view as CoordinatorLayout
        wallpaperOptionView!!.setOnWallpaperListener(mWallpaperListener)
        pageSettingsView!!.setPageListener(this)
        folderView!!.init(this)
        showDesktop()
        touchView!!.withDesktopInteractions(this)


        // update view
        // mDesktopView!!.setOnTouchListener(touchView)
    }

    fun handleLauncherResume() {
        if (ignoreResume) {
            // only triggers when a new activity is launched that should leave launcher state alone
            // uninstall package activity and pick widget activity
            ignoreResume = false
        } else {
            folderView!!.onMotionFolderClose()
            if (mDesktopView!!.pagerView!!.pages!!.size > 0) {
                if (mDesktopView!!.pagerView!!.currentPage != null && !mDesktopView!!.pagerView!!.currentPage!!.isBackgroundZoomed) {
                    // exit desktop edit mode
                    mDesktopView!!.pagerView!!.currentPage!!.performClick()
                } else if (mDesktopView!!.pagerView!!.currentItem != 0) {
                    // set desktop to first page
                    mDesktopView!!.pagerView!!.currentItem = 0
                }
            }
        }
    }

    override fun onStart() {
        if (appWidgetHost != null) {
            appWidgetHost!!.startListening()
        }
        super.onStart()
    }

    fun setWidgetItem(request: Int, result: Int, appWidgetID: Int) {
        if (result == Activity.RESULT_OK) {
            // data
            /*
            // deprecated
            AppWidgetProviderInfo appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetID);
            if (appWidgetInfo.configure != null && request == REQUEST_PICK_APPWIDGET) {
                Intent intent = new Intent("android.appwidget.action.APPWIDGET_CONFIGURE")
                        .setComponent(appWidgetInfo.configure)
                        .setClass(HomeActivity.launcher, HomeActivity.class)
                        .putExtra("appWidgetId", appWidgetID);

                 startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);

            } else */
            if (request == PICK_APPWIDGET /*|| request == HomeActivity.REQUEST_CREATE_APPWIDGET*/) {
                // create item
                val item = ItemWidgetView(this, appWidgetID)
                ItemViewManager.setInDesktop(null, item)

                // back to Desktop
                for (`object` in pagerView!!.pages!!) {
                    val page = `object` as CellContainer
                    page.onAnimateZoom(1.0f)
                }
                showDesktop()
            }

            // mDesktopFragment.onResponseWidget(requestCode, data);
        } else if (result == Activity.RESULT_CANCELED && appWidgetID != -1) {
            appWidgetHost!!.deleteAppWidgetId(appWidgetID)
        }
    }

    override fun onPickWidget() {
        ignoreResume = true
        val appWidgetId = appWidgetHost!!.allocateAppWidgetId()
        val pickIntent = Intent()
                .setAction(ACTION_APPWIDGET_PICK)
                .putExtra(EXTRA_APPWIDGET_ID, appWidgetId)

        mHomeActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            mHomeActivity.onActivityResult(PICK_APPWIDGET, result)
        }.launch(pickIntent)
    }

    override fun onLaunchWallpaper() {
        showWallpaperEdit()
    }

    override fun onLaunchSettings() {
        // LauncherAction.RunAction(LauncherAction.Action.LAUNCHER_SETTINGS, this.getContext());
    }

    override fun onDestroy() {
        appWidgetManager = null
        appWidgetHost!!.stopListening()
        super.onDestroy()
    }

    fun onEditWallpaper() {
        val colorIndicator = Display.AverageColor.pageIndicator
        val colorDesktopOptions = Display.AverageColor.desktopOptions
        // val indicatorValue = Tool.getAverageColorValue(colorIndicator)
        val desktopValue = Tool.getAverageColorValue(colorDesktopOptions)
        // mDesktopView!!.pageIndicatorView!!.setPaintColor(indicatorValue)
        pageSettingsView!!.setPaintColor(desktopValue)
    }

    override fun showDesktop() {
        Tool.goneViews(100, wallpaperOptionView)
        Tool.goneViews(100, pageSettingsView)
        mDesktopView!!.onDesktop()
    }

    fun showDesktopEdit() {
        Tool.goneViews(100, wallpaperOptionView)
        Tool.visibleViews(100, pageSettingsView)
        mDesktopView!!.onDesktopEdit()
    }

    fun showWallpaperEdit() {
        Tool.visibleViews(100, wallpaperOptionView)
        Tool.goneViews(100, pageSettingsView)
        mDesktopView!!.onWallpaperLayout()
    }

    fun setAlpha(positionY: Float) {
        var positionY = positionY
        val alphaArea = Display.DISPLAY_SIZE!!.height * MAX_ALPHA_INTERACTION

        // alpha
        if (positionY < Display.DISPLAY_SIZE!!.height - alphaArea) {
            mAlpha = 0.000f
        } else {
            positionY = Display.DISPLAY_SIZE!!.height - positionY
            if (positionY <= alphaArea) {
                mAlpha = 1.000f - positionY / alphaArea
            }
        }
        mInstance!!.alpha = mAlpha
        mDesktopView!!.requestLayout()
    }

    fun init() {
        // pagerView!!.init(this)
        dockView!!.init(this)

        // mDesktopView!!.pageIndicatorView!!.setOnFingerListener(mMenuFragment!!)
    }

    val instance: View?
        get() = mInstance
    val pagerView: PagerView?
        get() = mDesktopView!!.pagerView
    val dockView: DockView?
        get() = mDesktopView!!.dockView
    var fragmentSwitcher: FragmentSwitcher
        get() = mFragmentSwitcher!!
        set(switcher) {
            mFragmentSwitcher = switcher
            mMenuFragment = switcher.menuFragment
        }

    companion object {
        // finals
        private val TAG = DesktopFragment::class.java.simpleName
        private const val MAX_ALPHA_INTERACTION = 0.25f // 25% of the screen

        // models
        var appWidgetManager: AppWidgetManager? = null
        var appWidgetHost: WidgetHost? = null

        // variables
        var ignoreResume = false
    }

    init {
        mWallpaperListener = homeActivity
        mHomeActivity = homeActivity
        this.touchView = touchView
    }
}