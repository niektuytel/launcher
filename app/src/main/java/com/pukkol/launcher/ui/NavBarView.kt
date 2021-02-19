package com.pukkol.launcher.ui

import android.content.Context
import android.util.AttributeSet
import android.view.*
import com.pukkol.launcher.ui.home.HomeActivity
import com.pukkol.launcher.util.Display

@Deprecated(level = DeprecationLevel.HIDDEN, message = "unused, will been deleted in near future")
class NavBarView(context: Context?, attr: AttributeSet?) : View(context, attr) {
    override fun onAttachedToWindow() {
        val navHeight = Display.NAVBAR_HEIGHT
        if (navHeight > 0) {
            val layoutParams = this.layoutParams
            layoutParams.height = navHeight
            setLayoutParams(layoutParams)
        }
        super.onAttachedToWindow()
    }
}