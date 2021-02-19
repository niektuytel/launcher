package com.pukkol.launcher.ui.home.desktop

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.pukkol.launcher.R
import com.pukkol.launcher.data.model.Item.Companion.getMinSize
import com.pukkol.launcher.ui.home.FragmentSwitcher.Companion.prevSwitchedState
import com.pukkol.launcher.ui.home.FragmentSwitcher.Companion.switchedState
import com.pukkol.launcher.ui.home.FragmentSwitcher.SwitchedState.IN_DESKTOP
import com.pukkol.launcher.ui.home.FragmentSwitcher.SwitchedState.IN_EDIT_DESKTOP
import com.pukkol.launcher.ui.home.TouchDragHelper.DragMode.DRAG_MENU_MODE
import com.pukkol.launcher.util.Display
import com.pukkol.launcher.util.Tool
import kotlin.math.floor

/**
 * #variables:
 * [pagerView] content of all pages
 * [dockView] content od dock view
 *
 * #functions:
 * [onCreateLayout] create this layout
 * [onCreatePage] create a new CellContainer for pager
 * [onDesktop] display Desktop
 * [onDesktopEdit] display Desktop edit
 * [onWallpaperLayout] display wallpaper layout
 **/
class DesktopLayout : LinearLayout {
    var pagerView: PagerView? = null
        private set

    var dockView: DockView? = null
        private set

    private var pageIndicatorView: PageIndicatorView? = null
    private var _desktopFragment: DesktopFragment? = null
    private lateinit var pagerHelper : PagerHelper

    constructor(context: Context?) : super(
            context,
            null,
            0
    )
    constructor(context: Context?, attrs: AttributeSet?) : super(
            context,
            attrs,
            0
    )
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
    )

    fun onCreateLayout(desktopFragment: DesktopFragment) {
        _desktopFragment = desktopFragment
        setPaddingRelative(0, Display.STATUSBAR_HEIGHT, 0, 0)
        val heightDock = getMinSize(context, true).height
        val heightpagination = this.resources.getDimensionPixelSize(R.dimen.page_indicator_height)
        var heightPager =
                Display.DISPLAY_SIZE!!.height -
                Display.STATUSBAR_HEIGHT -
                heightpagination - heightDock -
                Display.NAVBAR_HEIGHT

        // API 29+ you need as well to add the status Height to get the Display size
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            heightPager += Display.STATUSBAR_HEIGHT
        }

        // layoutParams views
        val pagerParams = ViewGroup.LayoutParams(Display.DISPLAY_SIZE!!.width, heightPager)
        val paginationParams = ViewGroup.LayoutParams(Display.DISPLAY_SIZE!!.width, heightpagination)
        val dockParams = ViewGroup.LayoutParams(Display.DISPLAY_SIZE!!.width, heightDock)

        // views
        dockView = DockView(context, null, dockParams)
        pageIndicatorView = PageIndicatorView(context, null, paginationParams)
        pagerView = PagerView(context, null, pagerParams, desktopFragment, this, pageIndicatorView!!)

        this.addView(pagerView)
        this.addView(pageIndicatorView)
        this.addView(dockView)

        // add interactions
        setTouchListeners(this)
        pagerHelper = PagerHelper(pagerView!!)
    }

    fun onCreatePage(params: ViewGroup.LayoutParams): CellContainer {
        val layoutHeight = params.height
        val cellHeight = getMinSize(context, false).height
        val rasterVertical = floor(layoutHeight.toDouble() / cellHeight.toDouble()).toInt()
        val page = CellContainer(context, null, params, rasterVertical)

        setTouchListeners(page)
        return page
    }

    fun onDesktopEdit() {
        if (prevSwitchedState == IN_DESKTOP && _desktopFragment!!.touchView!!.dragHelper.dragMode != DRAG_MENU_MODE) {
            switchedState = IN_EDIT_DESKTOP
            prevSwitchedState = IN_EDIT_DESKTOP

            val targetScale = 0.9f
            for (page in pagerView!!.pages) {
                page.onAnimateZoom(targetScale)
            }

            onDesktopSettingsLayout()
        }
    }

    fun onDesktop() {
        if (prevSwitchedState == IN_EDIT_DESKTOP) {
            switchedState = IN_DESKTOP
            prevSwitchedState = IN_DESKTOP

            val targetScale = 1.0f
            for (page in pagerView!!.pages) {
                page.onAnimateZoom(targetScale)
            }

            onDesktopLayout()
        }
    }

    fun onWallpaperLayout() {
        Tool.goneViews(100, pagerView)
        Tool.goneViews(100, pageIndicatorView)
        Tool.goneViews(100, dockView)
    }

    private fun onDesktopLayout() {
        Tool.visibleViews(100, pagerView)
        Tool.visibleViews(100, pageIndicatorView)
        Tool.visibleViews(100, dockView)
    }

    private fun onDesktopSettingsLayout() {
        Tool.visibleViews(100, pagerView)
        Tool.invisibleViews(100, pageIndicatorView)
        Tool.invisibleViews(100, dockView)
    }

    private fun setTouchListeners(view : ViewGroup) {
        view.setOnTouchListener(_desktopFragment!!.touchView)
        view.setOnClickListener { _desktopFragment!!.showDesktop() }
        view.setOnLongClickListener { _desktopFragment!!.showDesktopEdit(); true }
    }

}