package com.pukkol.launcher.ui.home.desktop

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.pukkol.launcher.R
import com.pukkol.launcher.interfaces.IPageListener
import com.pukkol.launcher.util.Display
import com.pukkol.launcher.util.Tool

/**
 * @author okido (Niek Tuytel)[2/5/2021]
 * display settings screen of the desktop when you clicked long on the Desktop Page
 */
class PageSettingsView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr), View.OnClickListener {
    private var mPageListener: IPageListener? = null
    private val mTextWidget: TextView
    private val mTextWallpaper: TextView
    private val mTextSettings: TextView
    private val mImageWidget: ImageView
    private val mImageWallpaper: ImageView
    private val mImageSettings: ImageView
    fun setPageListener(desktopOptionListener: IPageListener?) {
        mPageListener = desktopOptionListener
    }

    fun setPaintColor(color: Int) {
        if (color > 225) {
            mTextWidget.setTextColor(Color.BLACK)
            mTextWallpaper.setTextColor(Color.BLACK)
            mTextSettings.setTextColor(Color.BLACK)
            mImageWidget.setImageResource(R.drawable.ic_widget_black)
            mImageWallpaper.setImageResource(R.drawable.ic_wallpaper_black)
            mImageSettings.setImageResource(R.drawable.ic_settings_black)
        } else {
            mTextWidget.setTextColor(Color.WHITE)
            mTextWallpaper.setTextColor(Color.WHITE)
            mTextSettings.setTextColor(Color.WHITE)
            mImageWidget.setImageResource(R.drawable.ic_widget_white)
            mImageWallpaper.setImageResource(R.drawable.ic_wallpaper_white)
            mImageSettings.setImageResource(R.drawable.ic_settings_white)
        }
    }

    override fun onClick(view: View) {
        if (mPageListener == null) return
        when (view.id) {
            R.id.layout_option_widget -> {
                mPageListener!!.onPickWidget()
            }
            R.id.layout_option_wallpaper -> {
                mPageListener!!.onLaunchWallpaper()
            }
            R.id.layout_option_settings -> {
                mPageListener!!.onLaunchSettings()
            }
        }
    }

    init {
        (getContext() as Activity).layoutInflater
                .inflate(R.layout.view_desktop_options, this, true)

        // set below status bar & above navigation bar
        setPadding(0, Display.STATUSBAR_HEIGHT, 0, 0)
        setPadding(0, 0, 0, Display.NAVBAR_HEIGHT)
        mTextWidget = findViewById(R.id.text_option_widget)
        mTextWallpaper = findViewById(R.id.text_option_wallpaper)
        mTextSettings = findViewById(R.id.text_option_settings)
        mImageWidget = findViewById(R.id.image_option_widget)
        mImageWallpaper = findViewById(R.id.image_option_wallpaper)
        mImageSettings = findViewById(R.id.image_option_settings)
        val layoutOptionWidget = findViewById<LinearLayout>(R.id.layout_option_widget)
        val layoutOptionWallpaper = findViewById<LinearLayout>(R.id.layout_option_wallpaper)
        val layoutOptionSettings = findViewById<LinearLayout>(R.id.layout_option_settings)
        val color = Display.AverageColor.desktopOptions
        val value = Tool.getAverageColorValue(color)
        setPaintColor(value)
        layoutOptionWidget.setOnClickListener(this)
        layoutOptionWallpaper.setOnClickListener(this)
        layoutOptionSettings.setOnClickListener(this)
    }
}