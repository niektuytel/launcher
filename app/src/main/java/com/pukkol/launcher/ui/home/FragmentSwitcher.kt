package com.pukkol.launcher.ui.home

import android.view.MotionEvent
import com.pukkol.launcher.ui.home.FragmentSwitcher.SwitchedState.*
import com.pukkol.launcher.ui.home.desktop.DesktopFragment
import com.pukkol.launcher.ui.home.menu.MenuFragment
import com.pukkol.launcher.util.Display
import kotlin.math.abs
/**
 * FragmentSwitcher os a class that handles the motion
 * between 2 fragments and connect them together.
 **/
class FragmentSwitcher(val menuFragment: MenuFragment, val desktopFragment: DesktopFragment) {
    val fingerInteraction = FingerInteraction()

    enum class SwitchedState { IN_DESKTOP, IN_MENU, IN_EDIT_DESKTOP, DRAG_FOR_MENU }

    companion object {
        var switchedState: SwitchedState = IN_DESKTOP
        var prevSwitchedState: SwitchedState = IN_DESKTOP
    }

    init {
        desktopFragment.fragmentSwitcher = this
        menuFragment.fragmentSwitcher = this
    }

    fun onBackPressed() {
        if (prevSwitchedState == IN_MENU) {
            menuFragment.onBackPressed()
        } else {
            desktopFragment.handleLauncherResume()
        }
    }

    fun onDestroy() {
        desktopFragment.onDestroy()
        menuFragment.onDetachView()
    }

    fun motionToDesktop() {
        if (prevSwitchedState == IN_MENU) {
            menuFragment.onMotionFragmentDown()
        }
    }

    fun setSwitchedState(position: Int) {
        val displayHeight = Display.DISPLAY_SIZE!!.height
        val inMenu = (prevSwitchedState == IN_MENU)

        // update current mode
        if (position == 0 && !inMenu) {
            prevSwitchedState = IN_MENU
            switchedState = IN_MENU
        } else if (position == displayHeight && inMenu) {
            prevSwitchedState = IN_DESKTOP
            switchedState = IN_DESKTOP
        } else {
            switchedState = DRAG_FOR_MENU
        }
    }

    /*
    * Finger interaction Handler
    */
    inner class FingerInteraction {
        private var displayHeight = Display.DISPLAY_SIZE!!.height
        private var latestY: Float
        private var dragUp: Boolean

        init {
            dragUp = prevSwitchedState == IN_DESKTOP
            latestY = if (dragUp) displayHeight.toFloat() else 0.toFloat()
        }

        fun onTouch(motionEvent: MotionEvent, startY: Float): Boolean {

            val action = motionEvent.action
            val fingerY = motionEvent.rawY

            if (startY == 0.00f && action != MotionEvent.ACTION_MOVE) {
                return false
            }

            // drag direction
            if (abs(fingerY - latestY) > 50) {
                dragUp = fingerY < latestY
                latestY = fingerY
            }

            // fragment position
            val dragY = fingerY - startY
            var currentPosition = if (prevSwitchedState == IN_DESKTOP) displayHeight.toFloat() else 0f
            currentPosition += dragY

            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    menuFragment.setMenuPosition(currentPosition)
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    autoMotion()
                    return false
                }
            }
            return false
        }

        private fun autoMotion() {
            if (dragUp) {
                menuFragment.onMotionFragmentUp()
            } else {
                menuFragment.onMotionFragmentDown()
            }
            dragUp = prevSwitchedState == IN_DESKTOP
            latestY = if (dragUp) displayHeight.toFloat() else 0f
        }
    }
}