package com.pukkol.launcher.util

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import com.pukkol.launcher.R
import com.pukkol.launcher.Setup

object Display {
    private val TAG = Display::class.java.simpleName
    var STATUSBAR_HEIGHT = 0
    var DISPLAY_SIZE: Size? = null
    var NAVBAR_HEIGHT = 0
    fun setSize(activity: Activity?) {
        if (activity == null) {
            DISPLAY_SIZE = null
            return
        }
        // update
        setStatusBarHeight(activity)
        setNavigationBarHeight(activity)
        val width: Int
        var height: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            width = windowMetrics.bounds.width() - insets.left - insets.right
            height = windowMetrics.bounds.height() - insets.top - insets.bottom
        } else {
            val displayMetrics = activity.resources.displayMetrics
            height = displayMetrics.heightPixels
            width = displayMetrics.widthPixels
        }

        // add bar is needed (https://stackoverflow.com/questions/4743116/get-screen-width-and-height-in-android)
        height += NAVBAR_HEIGHT
        DISPLAY_SIZE = Size(width, height)
    }

    private fun setStatusBarHeight(activity: Activity) {
        val resourceId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            STATUSBAR_HEIGHT = activity.resources.getDimensionPixelSize(resourceId)
        } else {
            STATUSBAR_HEIGHT = 0
        }
    }

    private fun setNavigationBarHeight(activity: Activity) {
        val metrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metrics)
        val usableHeight = metrics.heightPixels
        activity.windowManager.defaultDisplay.getRealMetrics(metrics)
        val realHeight = metrics.heightPixels
        if (realHeight > usableHeight) {
            NAVBAR_HEIGHT = realHeight - usableHeight
        } else {
            NAVBAR_HEIGHT = 0
        }
    }

    fun toast(context: Context, str: Int) {
        Toast.makeText(context, context.resources.getString(str), Toast.LENGTH_SHORT).show()
    }

    fun toast(context: Context?, str: String?) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
    }

    fun dp2px(dp: Float): Int {
        val resources = Resources.getSystem()
        val px = dp * resources.displayMetrics.density
        return Math.ceil(px.toDouble()).toInt()
    }

    fun sp2px(sp: Float): Int {
        val resources = Resources.getSystem()
        val px = sp * resources.displayMetrics.scaledDensity
        return Math.ceil(px.toDouble()).toInt()
    }

    /**
     * Get the TextView height before the TextView will render
     * @param textView the TextView to measure
     * @return the height of the textView
     */
    fun getTextViewHeight(textView: TextView): Int {
        val width = DISPLAY_SIZE!!.height
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        textView.measure(widthMeasureSpec, heightMeasureSpec)
        return textView.measuredHeight
    }

    fun vibrate(view: View) {
        val vibrator = view.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator?.vibrate(VibrationEffect.createOneShot(50, 160))
                ?: // some manufacturers do not vibrate on long press
                // might as well make this a fallback method
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    fun logAction(event: MotionEvent?) {
        if (event == null) return
        when (event.action) {
            MotionEvent.ACTION_CANCEL -> {
                Log.d(TAG, "MotionEvent.ACTION_CANCEL")
            }
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "MotionEvent.ACTION_DOWN")
            }
            MotionEvent.ACTION_MASK -> {
                Log.d(TAG, "MotionEvent.ACTION_MASK")
            }
            MotionEvent.ACTION_MOVE -> {
                Log.d(TAG, "MotionEvent.ACTION_MOVE")
            }
            MotionEvent.ACTION_UP -> {
                Log.d(TAG, "MotionEvent.ACTION_UP")
            }
        }
    }

    object AverageColor {
        fun setDisplay(context: Context?, wallpaper: Bitmap) {
            val displayHeight = DISPLAY_SIZE!!.height
            val bitmap = Bitmap.createBitmap(wallpaper, 0, 0, wallpaper.width, displayHeight)
            val average = Tool.averageBitmapColor(bitmap)
            Setup.deviceSettings().device().setWallpaperColor(average)
        }

        fun setSearchBar(context: Context, wallpaper: Bitmap) {
            val statusHeight = STATUSBAR_HEIGHT
            val menuMarginTop = context.resources.getDimensionPixelSize(R.dimen.menu_margin_top)
            val searchBoxMargin = context.resources.getDimensionPixelSize(R.dimen.menu_searchBox_margin)
            val searchBoxHeight = context.resources.getDimensionPixelSize(R.dimen.menu_searchBox_height)
            val y = statusHeight + menuMarginTop + searchBoxMargin
            val bitmap = Bitmap.createBitmap(wallpaper, 0, y, wallpaper.width, searchBoxHeight)
            val average = Tool.averageBitmapColor(bitmap)
            Setup.deviceSettings().device().setAreaSearchBoxColor(average)
        }

        fun setPageIndicator(context: Context, wallpaper: Bitmap) {
            val indicatorHeight = context.resources.getDimensionPixelSize(R.dimen.page_indicator_height)
            val dockHeight = context.resources.getDimensionPixelSize(R.dimen.dock_height)
            val dockMargin = context.resources.getDimensionPixelSize(R.dimen.dock_margin)
            val navBarHeight = NAVBAR_HEIGHT
            val displayHeight = DISPLAY_SIZE!!.height // setDisplaySize(context).getHeight();
            val y = displayHeight - navBarHeight - dockMargin * 2 - dockHeight - indicatorHeight
            val bitmap = Bitmap.createBitmap(wallpaper, 0, y, wallpaper.width, indicatorHeight)
            val average = Tool.averageBitmapColor(bitmap)
            Setup.deviceSettings().device().setAreaPageIndicatorColor(average)
        }

        fun setDesktopOptions(context: Context, wallpaper: Bitmap) {
            val desktopOptionsHeight = context.resources.getDimensionPixelSize(R.dimen.desktop_options_height)
            val navBarHeight = NAVBAR_HEIGHT //getNavigationBarHeight(context);
            val displayHeight = DISPLAY_SIZE!!.height
            val y = displayHeight - navBarHeight - desktopOptionsHeight
            val bitmap = Bitmap.createBitmap(wallpaper, 0, y, wallpaper.width, desktopOptionsHeight)
            val average = Tool.averageBitmapColor(bitmap)
            Setup.deviceSettings().device().setAreaDesktopOptionsColor(average)
        }

        val display: Color
            get() = Setup.deviceSettings().device().wallpaperColor
        val searchBox: Color
            get() = Setup.deviceSettings().device().areaSearchBoxColor
        val pageIndicator: Color
            get() = Setup.deviceSettings().device().areaPageIndicatorColor
        val desktopOptions: Color
            get() = Setup.deviceSettings().device().areaDesktopOptionsColor
    }

    object Hide {
        fun keyboard(context: Context, view: View) {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    ?: return
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }

        fun statusBar(activity: Activity?) {
            if (activity == null) return
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }

        fun navigationBar(activity: Activity?) {
            if (activity == null) return

            //for new api versions.
            val decorView = activity.window.decorView
            val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            decorView.systemUiVisibility = uiOptions
        }
    }

    object Show {
        fun keyboard(context: Context, view: View) {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    ?: return
            inputMethodManager.toggleSoftInputFromWindow(view.windowToken, InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS)
        }

        fun statusBar(activity: Activity?) {
            if (activity == null) return
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        fun navigationBar(activity: Activity?) {
            if (activity == null) return


            //for new api versions.
            val decorView = activity.window.decorView
            val uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            decorView.systemUiVisibility = uiOptions
        }
    }
}