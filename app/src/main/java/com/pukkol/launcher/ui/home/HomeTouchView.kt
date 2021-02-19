package com.pukkol.launcher.ui.home

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.pukkol.launcher.data.model.Item
import com.pukkol.launcher.ui.home.desktop.DesktopFragment

class HomeTouchView(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs), View.OnTouchListener {
    val dragHelper = TouchDragHelper(this)

//    // interact between on Option and on Item Drag
//    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
//        return dragHelper.onInterceptTouchEvent(event!!) || super.onInterceptTouchEvent(event)
//    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return dragHelper.onTouch(v!!, event!!) || super.onTouchEvent(event)
    }

    fun withDesktopInteractions(desktopFragment: DesktopFragment) {
        dragHelper.withDesktopFragment(desktopFragment)
    }

    fun onStartDragItem(item: Item) {
        dragHelper.onItemDragStart(item)
    }
}