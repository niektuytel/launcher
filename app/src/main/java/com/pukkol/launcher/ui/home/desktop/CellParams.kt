package com.pukkol.launcher.ui.home.desktop

import android.graphics.Point
import android.graphics.Rect
import android.util.Size
import android.view.ViewGroup

class CellParams(layoutWidth: Int, layoutHeight: Int, private val mBorderPosition: Point, private val mBorderSize: Size) : ViewGroup.LayoutParams(layoutWidth, layoutHeight) {
    var borderX: Int
        get() = mBorderPosition.x
        set(v) {
            mBorderPosition.x = v
        }
    var borderY: Int
        get() = mBorderPosition.y
        set(v) {
            mBorderPosition.y = v
        }
    var borderWidth: Int
        get() = mBorderSize.width
        set(width) {
            super.width = width
        }
    var borderHeight: Int
        get() = mBorderSize.height
        set(height) {
            super.height = height
        }
    val border: Rect
        get() = Rect(
                mBorderPosition.x,
                mBorderPosition.y,
                mBorderPosition.x + mBorderSize.width,
                mBorderPosition.y + mBorderSize.height
        )

}