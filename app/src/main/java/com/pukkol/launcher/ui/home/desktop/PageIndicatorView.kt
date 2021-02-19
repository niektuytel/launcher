package com.pukkol.launcher.ui.home.desktop

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.ViewPager
import com.pukkol.launcher.R
import com.pukkol.launcher.util.Display
import java.lang.Math.toRadians
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * [setViewPager] set pager view to layout
 * [onShowIndicator] display pager line on given position
 * */
@SuppressLint("ViewConstructor")
class PageIndicatorView(context: Context?, attrs: AttributeSet?, params: ViewGroup.LayoutParams?) : View(context, attrs), ViewPager.OnPageChangeListener {
    companion object {
        private val EXPAND_SIZE = Display.dp2px(10f)
    }

    private var pager: ViewPager? = null
    private var cursorPath: Path? = null
    private val paint = Paint()
    private var scrollOffset = 0f
    private var bounce = 0f
    private var paintAlpha = 0
    private var scrollPosition = 0
    private var arrowX1 = 0
    private var arrowY1 = 0
    private var arrowX2 = 0
    private var arrowY2 = 0

    init {
        this.also {
            it.layoutParams = params
            it.background = ResourcesCompat.getDrawable(resources, R.drawable.overlay_page_indicator, null)
        }

        paint.also {
            it.strokeWidth = 3f
            it.isAntiAlias = true
            it.color = Color.WHITE
            it.style = Paint.Style.STROKE
        }
    }

    fun setViewPager(pagerView: ViewPager?) {
        if (pagerView == null && pager != null) {
            pager!!.removeOnPageChangeListener(this)
            pager = null
        } else {
            pager = pagerView
            if (pager != null) {
                pager!!.addOnPageChangeListener(this)
            }
        }
        invalidate()
    }

    fun onShowIndicator(offset: Float, bounce: Float) {
        if (offset == 0.00f) {
            if (lineIsVisible()) {
                hideLine()
            }
        } else if (!lineIsVisible() || bounce != 0f) {
            showLine(bounce)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val centerX = this.width / 2
        val centerY = this.height / 2
        arrowX1 = centerX - EXPAND_SIZE
        arrowX2 = centerX + EXPAND_SIZE
        arrowY1 = centerY + EXPAND_SIZE / 2
        arrowY2 = centerY + EXPAND_SIZE / 2
        curvedCursor()
    }

    override fun onDraw(canvas: Canvas) {
        if (pager == null || pager!!.adapter == null) {
            return
        }

        val pageIndex = pager!!.adapter!!.count
        paint.alpha = 255
        canvas.drawPath(cursorPath!!, paint)
        drawLine(canvas, pageIndex)
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        scrollOffset = positionOffset
        scrollPosition = position
        invalidate()
    }

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onPageSelected(position: Int) {}

    private fun drawLine(canvas: Canvas?, pageIndex: Int) {
        if (canvas == null) return

        // draw page indicator line
        paint.alpha = max(paintAlpha, 0)
        val lineSize = this.width.toFloat() / pageIndex
        val startX = (scrollPosition + scrollOffset) * lineSize - bounce
        val stopX = startX + lineSize
        var aroundExpand = false

        if (startX < arrowX1 && stopX > arrowX1) {
            canvas.drawLine(startX, arrowY1.toFloat(), arrowX1.toFloat(), arrowY2.toFloat(), paint)
            aroundExpand = true
        }

        if (startX < arrowX2 && stopX > arrowX2) {
            canvas.drawLine(arrowX2.toFloat(), arrowY1.toFloat(), stopX, arrowY2.toFloat(), paint)
            aroundExpand = true
        }

        if (!aroundExpand) {
            canvas.drawLine(startX, arrowY1.toFloat(), stopX, arrowY2.toFloat(), paint)
        }

        if (scrollOffset != 0f) {
            invalidate() // re-draw
        }
    }

    private fun showLine(bounce: Float) {
        this.bounce = bounce
        paintAlpha = 255
        invalidate()
    }

    private fun hideLine() {
        Thread {
            while (paintAlpha > 0) {
                paintAlpha -= 2
                invalidate()
                try {
                    Thread.sleep(5)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    private fun lineIsVisible(): Boolean {
        return paintAlpha == 255
    }

    private fun curvedCursor() {
        cursorPath = Path()
        val curveRadius = 20
        val midX = arrowX1 + (arrowX2 - arrowX1) / 2
        val midY = arrowY1 + (arrowY2 - arrowY1) / 2
        val xDiff = (midX - arrowX1).toFloat()
        val yDiff = (midY - arrowY1).toFloat()
        val angle = atan2(yDiff.toDouble(), xDiff.toDouble()) * (180 / Math.PI) - 90
        val angleRadians = toRadians(angle)
        val pointX = (midX + curveRadius * cos(angleRadians)).toFloat()
        val pointY = (midY + curveRadius * sin(angleRadians)).toFloat()
        cursorPath!!.moveTo(arrowX1.toFloat(), arrowY1.toFloat())
        cursorPath!!.cubicTo(arrowX1.toFloat(), arrowY1.toFloat(), pointX, pointY, arrowX2.toFloat(), arrowY2.toFloat())
    }

}