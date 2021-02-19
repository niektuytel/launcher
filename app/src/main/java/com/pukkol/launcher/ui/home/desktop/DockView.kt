package com.pukkol.launcher.ui.home.desktop

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.pukkol.launcher.Setup

@SuppressLint("ViewConstructor")
class DockView(context: Context?, attr: AttributeSet?, params: LayoutParams?) : CellContainer(context, attr, params, 1) {
    private lateinit var mDesktopFragment: DesktopFragment

    @SuppressLint("ClickableViewAccessibility")
    fun init(desktopFragment: DesktopFragment) {
        mDesktopFragment = desktopFragment
        val dockItems = Setup.itemManager().getDockItems(desktopFragment, this)
        // removePageItems();
        for (item in dockItems!!) {
            setItem(item!!)
        }
        this.invalidate()
    }
}