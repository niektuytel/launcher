package com.pukkol.launcher.ui.itemview

import android.annotation.SuppressLint
import android.appwidget.AppWidgetHostView
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import com.pukkol.launcher.ui.home.HomeActivity
import com.pukkol.launcher.util.Display

class WidgetHostView(context: Context?) : AppWidgetHostView(context) {
    private var onTouchListener: OnTouchListener? = null
    private var mLongClick: OnLongClickListener? = null
    private var mInteracting = false
    private var mFirstTimestamp: Long = 0
    private var mFirstPoint = Point(0, 0)
    private var mFingerPoint = Point(0, 0)

    @SuppressLint("ClickableViewAccessibility")
    override fun setOnTouchListener(onTouchListener: OnTouchListener) {
        // super.setOnTouchListener(onTouchListener)
        this.onTouchListener = onTouchListener
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        mLongClick = l
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        val fingerX = event.x.toInt()
        val fingerY = event.y.toInt()
        mFingerPoint = Point(fingerX, fingerY)
        if (event.action == MotionEvent.ACTION_DOWN) {
            mFirstTimestamp = System.currentTimeMillis()
            mFirstPoint = mFingerPoint
            mInteracting = true
        }

        Thread { threadOnLongClick() }.start()
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            mInteracting = false
        }

        // set on Touch
        if (onTouchListener != null && (event.rawX > 0 && event.rawY > 0)) {
            return onTouchListener!!.onTouch(this, event)
        }
        return super.onInterceptTouchEvent(event)
    }

    private fun threadOnLongClick() {
        var now: Long
        var thresholdTime: Long
        while (mInteracting && inThreshold()) {
            now = System.currentTimeMillis()
            thresholdTime = mFirstTimestamp + TIMESTAMP_THRESHOLD
            if (thresholdTime < now) {
                HomeActivity.launcher!!.runOnUiThread { mLongClick!!.onLongClick(this) }
                break
            }
        }
        mInteracting = false
    }

    private fun inThreshold(): Boolean {
        val border = Rect()
        border.left = mFirstPoint.x - DRAG_THRESHOLD
        border.top = mFirstPoint.y - DRAG_THRESHOLD
        border.right = mFirstPoint.x + DRAG_THRESHOLD
        border.bottom = mFirstPoint.y + DRAG_THRESHOLD
        return mFingerPoint.x > border.left && mFingerPoint.x < border.right && mFingerPoint.y > border.top && mFingerPoint.y < border.bottom
    }

    companion object {
        private const val TIMESTAMP_THRESHOLD: Long = 400 // 400 mili seconds
        private val DRAG_THRESHOLD = Display.dp2px(10f) // 10dp
    }
}