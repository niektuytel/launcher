package com.pukkol.launcher.interfaces

import android.view.MotionEvent

interface OnTouchListener {
    //fun onInterceptTouchEvent(event: MotionEvent?): Boolean
    // fun onTouchItem(event: MotionEvent?): Boolean
    fun onBackPressed()
}