package com.pukkol.launcher.ui.itemview

import android.annotation.SuppressLint
import android.appwidget.AppWidgetProviderInfo
import android.database.Cursor
import android.graphics.Point
import android.util.Size
import android.view.*
import android.widget.LinearLayout
import com.pukkol.launcher.data.model.Item
import com.pukkol.launcher.ui.home.desktop.CellContainer
import com.pukkol.launcher.ui.home.desktop.CellParams
import com.pukkol.launcher.ui.home.desktop.DesktopFragment
import kotlin.math.max

@Suppress("DIFFERENT_NAMES_FOR_THE_SAME_PARAMETER_IN_SUPERTYPES")
@SuppressLint("ViewConstructor")
class ItemWidgetView : Item, View.OnLongClickListener {
    private var mDesktopFragment: DesktopFragment
    private var isDragging = false

    @SuppressLint("ClickableViewAccessibility")
    constructor(desktopFragment: DesktopFragment, widgetID: Int) : super(desktopFragment.requireContext()) {
        mDesktopFragment = desktopFragment
        setPageCurrentEvent(currentPage, Location.DESKTOP)

        // Default data
        val appWidgetInfo: AppWidgetProviderInfo = DesktopFragment.appWidgetManager!!.getAppWidgetInfo(widgetID)
        childSize = Size(
                appWidgetInfo.minWidth,
                appWidgetInfo.minHeight
        )
        parentSize = Size(
                max(1, getCellCeilX(childSize!!.width)),
                max(1, getCellCeilY(childSize!!.height))
        )

        for (page in mDesktopFragment.pagerView!!.pages) {
            val position = page.getEmptySpan(parentSize!!)
            if (position != null) {
                parentPosition = position
                break
            }
        }

        childPosition = Point(
                ((getPixelX(parentSize!!.width) - childSize!!.width) / 2),
                ((getPixelY(parentSize!!.height) - childSize!!.height) / 2)
        )

        // item view
        ID = widgetID
        type = Type.WIDGET
        labelVisible = false

        // // NOT needed
        // var label: String? = appWidgetInfo.provider.packageName + Definitions.DELIMITER + appWidgetInfo.provider.className
        // label = label
        // labelColor = Color.WHITE
        // childSize = childSize
        // parentSize = parentSize
        // childPosition = childPoint
        // parentPosition = parentPoint

    }

    @SuppressLint("ClickableViewAccessibility")
    constructor(desktopFragment: DesktopFragment, cursor: Cursor, layout: Any?) : super(desktopFragment, cursor, layout) {
        mDesktopFragment = desktopFragment
    }


    override val currentPage: CellContainer
        get() = mDesktopFragment.pagerView!!.currentPage!!
    override val pageIndex: Int
        get() = mDesktopFragment.pagerView!!.currentPageIndex

    var onLongClicked = false

    override fun onLongClick(view: View): Boolean {
        if (!isDragging) {
            mDesktopFragment.touchView!!.onStartDragItem(this)
            onLongClicked = true
            return true
        }
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setItemView() {
        layoutParams = CellParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, parentPosition!!, parentSize!!)
        val childPoint = childPosition!!
        val childSize = childSize!!
        val appWidgetInfo: AppWidgetProviderInfo = DesktopFragment.appWidgetManager!!.getAppWidgetInfo(ID)
        val widgetHostView = DesktopFragment.appWidgetHost!!.createView(this.context, ID, appWidgetInfo) as WidgetHostView
        widgetHostView.setAppWidget(ID, appWidgetInfo)
        widgetHostView.setOnLongClickListener(this)
         widgetHostView.setOnTouchListener(mDesktopFragment.touchView!!)
        widgetHostView.layoutParams = ViewGroup.LayoutParams(
                childSize.width,
                childSize.height
        )
        widgetHostView.x = childPoint.x.toFloat()
        widgetHostView.y = childPoint.y.toFloat()

        // set View
        val childParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val fl = LinearLayout(this.context)
        fl.addView(widgetHostView)

        // update view
        removeAllViews()
        this.addView(fl, childParams)
    }

    override fun updateView() {}
}