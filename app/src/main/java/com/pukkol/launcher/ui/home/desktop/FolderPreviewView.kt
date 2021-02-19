package com.pukkol.launcher.ui.home.desktop

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.pukkol.launcher.R
import com.pukkol.launcher.ui.home.desktop.FolderPreviewView
import com.pukkol.launcher.util.Tool

/**
 * @author okido
 * @since 2021
 * <>
 * draw folder preview by given position:
 * onPreview(CellContainer page, Point position)
 *
 * remove folder preview:
 * onPreview(CellContainer page, Point position)
 * >
 */
class FolderPreviewView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val mPaint: Paint
    private val mLocation = Point()
    private var mScale = 1.0f
    var isOnPreview = false
        private set
    private val mIconSize: Int
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isOnPreview) return
        if (!mLocation.equals(-1, -1)) {
            mScale += 0.08f
            mScale = Tool.clampFloat(mScale, 0.5f, 1.0f)
            val radius = mIconSize * mScale
            canvas.drawCircle(mLocation.x.toFloat(), mLocation.y.toFloat(), radius, mPaint)
        }
        this.invalidate()
    }

    fun onPreview(page: CellContainer, position: Point) {
        val dropX = page.cellWidth * (position.x + 0.5f)
        val dropY = page.cellHeight * (position.y + 0.5f)
        try {
            showAt(page, PointF(dropX, dropY))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var mPrevLocation: Point? = null
    fun cancel(forced: Boolean) {
        if (forced) {
            isOnPreview = false
            onRedraw()
            mLocation[-1] = -1
        } else if (mPrevLocation !== mLocation) {
            isOnPreview = false
            onRedraw()
            mLocation[-1] = -1
        }
    }

    private fun showAt(fromView: View, position: PointF) {
        if (!isOnPreview) {
            isOnPreview = true
            mScale = 0.0f
            convertLocation(fromView, this, position)
            onRedraw()
        }
    }

    private fun onRedraw() {
        if (isOnPreview) {
            this.invalidate()
        } else {
            Log.d(TAG, "onRedraw() = false")
        }
    }

    private fun convertLocation(fromView: View, toView: View, position: PointF) {
        val fromPoint = IntArray(2)
        val toPoint = IntArray(2)
        fromView.getLocationOnScreen(fromPoint)
        toView.getLocationOnScreen(toPoint)
        val locationX = (fromPoint[0] - toPoint[0] + position.x).toInt()
        val locationY = (fromPoint[1] - toPoint[1] + position.y).toInt()
        mLocation[locationX] = locationY
        mPrevLocation = Point(locationX, locationY)
    }

    companion object {
        private val TAG = FolderPreviewView::class.java.simpleName
    }

    init {
        mIconSize = context.resources.getDimensionPixelSize(R.dimen.app_icon_width)
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.isFilterBitmap = true
        mPaint.color = Color.WHITE
        mPaint.alpha = 230
    }
}