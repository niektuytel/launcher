package com.pukkol.launcher.ui

import android.content.Context
import android.util.AttributeSet
import android.view.*
import com.pukkol.launcher.ui.home.HomeActivity
import com.pukkol.launcher.util.Display

@Deprecated(level = DeprecationLevel.HIDDEN, message = "unused, will been deleted in near future")
class StatusBarView(context: Context?, attr: AttributeSet?) : View(context, attr) {
    public override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {}
    override fun onAttachedToWindow() {
        val statusHeight = Display.STATUSBAR_HEIGHT
        if (statusHeight > 0) {
            val layoutParams = this.layoutParams
            layoutParams.height = statusHeight
            setLayoutParams(layoutParams)
        }
        super.onAttachedToWindow()
    }
}