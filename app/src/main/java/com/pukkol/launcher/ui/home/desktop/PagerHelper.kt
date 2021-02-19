package com.pukkol.launcher.ui.home.desktop

import android.graphics.Point
import android.os.Handler
import android.os.Looper
import android.view.View
import com.pukkol.launcher.R
import com.pukkol.launcher.ui.home.HomeActivity
import com.pukkol.launcher.util.Display
import com.pukkol.launcher.util.Tool
import kotlinx.android.synthetic.main.activity_home.*

/**
 * #functions:
 * [setVisibleState] display views of the possible paginations
 * [onInteract] check if finger is on 1 of the views and animate pagination
 * [onDestroyView] remove all views that surgest pagination motion
 */
class PagerHelper(private var pagerView: PagerView) {
    companion object {
        private const val NO_ALPHA = 0.0f
        private const val NORMAL_ALPHA = 0.5f
        private const val FLASH_ALPHA = 0.9f
    }

    enum class LightningState {
        LIGHTNING_ON, LIGHTNING_OFF, LIGHTNING_GONE
    }

    private val fieldLeft = HomeActivity.launcher!!.view_dragField_left
    private val fieldRight = HomeActivity.launcher!!.view_dragField_right
    private val fieldColor = Display.AverageColor.display
    private val handlerLeft = Handler(Looper.getMainLooper())
    private val handlerRight = Handler(Looper.getMainLooper())
    private var onAnimationLeft = false
    private var onAnimationRight = false

    init {
        // update color type
        val value = Tool.getAverageColorValue(fieldColor)
        if (value > 225) {
            fieldLeft.setBackgroundResource(R.drawable.drag_field_left_black)
            fieldRight.setBackgroundResource(R.drawable.drag_field_right_black)
        } else {
            fieldLeft.setBackgroundResource(R.drawable.drag_field_left_white)
            fieldRight.setBackgroundResource(R.drawable.drag_field_right_white)
        }
    }

    fun setVisibleState(pages: List<CellContainer>, currentPage: Int) {
        val totalPages = pages.size - 1 // size -> length

        val page = pages[currentPage]
        if (page.isEmpty) {
            leftFieldVisibleState(currentPage != 0)
            rightFieldVisibleState(currentPage != totalPages)
        } else {
            when {
                fieldLeft.alpha == NO_ALPHA -> {
                    rightFieldVisibleState(true)
                    leftFieldVisibleState(false)
                }
                fieldRight.alpha == NO_ALPHA -> {
                    rightFieldVisibleState(false)
                    leftFieldVisibleState(true)
                }
                else -> {
                    rightFieldVisibleState(true)
                    leftFieldVisibleState(true)
                }
            }
        }
    }

    fun onInteract(finger: Point, page: Any) {
        val inDockView = page is DockView

        // pagination
        if (inDockView || !isViewContains(finger, fieldLeft) && !isViewContains(finger, fieldRight)) {
            onAnimationLeft = false
            onAnimationRight = false
        } else if (isViewContains(finger, fieldLeft) && !onAnimationLeft) {
            onAnimationLeft = true
            onAnimationRight = false
            handlerLeft.removeCallbacksAndMessages(null)
            handlerLeft.post { pagerView.handleToPreviousPage() }
        } else if (isViewContains(finger, fieldRight) && !onAnimationRight) {
            onAnimationLeft = false
            onAnimationRight = true
            handlerRight.removeCallbacksAndMessages(null)
            handlerRight.post { pagerView.handleToNextPage() }
        }

        // UI
        if (inDockView) {
            flashOnState(fieldLeft, LightningState.LIGHTNING_GONE)
        } else if (!onAnimationLeft && !onAnimationRight) {
            flashOnState(fieldLeft, LightningState.LIGHTNING_OFF)
        } else if (!onAnimationLeft) {
            flashOnState(fieldRight, LightningState.LIGHTNING_ON)
        } else if (!onAnimationRight) {
            flashOnState(fieldLeft, LightningState.LIGHTNING_ON)
        }
    }

    fun onDestroyView() {
        fieldLeft.animate().alpha(NO_ALPHA)
        fieldRight.animate().alpha(NO_ALPHA)
        rightFieldVisibleState(false)
        leftFieldVisibleState(false)
    }

    private fun leftFieldVisibleState(visible: Boolean) {
        if (visible && fieldLeft.visibility == View.GONE) {
            fieldLeft.visibility = View.VISIBLE
        } else if (!visible && fieldLeft.visibility == View.VISIBLE) {
            fieldLeft.visibility = View.GONE
        }
    }

    private fun rightFieldVisibleState(visible: Boolean) {
        if (visible && fieldRight.visibility == View.GONE) {
            fieldRight.visibility = View.VISIBLE
        } else if (!visible && fieldRight.visibility == View.VISIBLE) {
            fieldRight.visibility = View.GONE
        }
    }

    private fun flashOnState(fieldView: View, lightningState: LightningState) {
        when (lightningState) {
            LightningState.LIGHTNING_ON -> {
                if (fieldView.id == fieldLeft.id) {
                    fieldLeft.animate().alpha(FLASH_ALPHA)
                    fieldRight.animate().alpha(NORMAL_ALPHA)
                }
                if (fieldView.id == fieldRight.id) {
                    fieldLeft.animate().alpha(NORMAL_ALPHA)
                    fieldRight.animate().alpha(FLASH_ALPHA)
                }
            }
            LightningState.LIGHTNING_OFF -> {
                fieldLeft.animate().alpha(NORMAL_ALPHA)
                fieldRight.animate().alpha(NORMAL_ALPHA)
            }
            LightningState.LIGHTNING_GONE -> {
                fieldLeft.animate().alpha(NO_ALPHA)
                fieldRight.animate().alpha(NO_ALPHA)
            }
        }
    }

    private fun isViewContains(finger: Point, fieldView: View): Boolean {
        val viewPoint = IntArray(2)
        fieldView.getLocationOnScreen(viewPoint)

        return ((finger.x >= viewPoint[0] && finger.x <= viewPoint[0] + fieldView.width)// X
                        &&
                (finger.y >= viewPoint[1] && finger.y <= viewPoint[1] + fieldView.height))// Y
    }
}