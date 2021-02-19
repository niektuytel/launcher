package com.pukkol.launcher.data.model

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.util.Size
import android.view.ViewGroup
import android.widget.LinearLayout
import com.pukkol.launcher.R
import com.pukkol.launcher.data.local.db.DBItemProfile
import com.pukkol.launcher.ui.home.desktop.CellContainer
import com.pukkol.launcher.ui.home.desktop.DesktopFragment
import com.pukkol.launcher.ui.home.desktop.DockView
import com.pukkol.launcher.ui.itemview.FolderDrawable
import com.pukkol.launcher.ui.itemview.ItemAppView
import com.pukkol.launcher.util.Display
import com.pukkol.launcher.util.Tool
import com.pukkol.launcher.viewutil.ConstraintLayoutView
import io.codetail.widget.RevealLinearLayout

/**
 * Item model represents a data structure of every Type @App that is been display on the screen,
 * to draw the correct type Item App on the screen :
 * - App
 * - Folder
 * - WidgetHost etc.
 *
 * @Author Niek Tuytel (Okido)
 */
abstract class Item : ConstraintLayoutView {
    companion object {
        private val DRAG_THRESHOLD = Display.dp2px(10f)
        const val REMOVE_PADDING_ON_SCALE = 2.0000
        @JvmField
        val PADDING_LEFT = Display.dp2px(20f)
        @JvmField
        val PADDING_TOP = Display.dp2px(20f)
        @JvmField
        val PADDING_RIGHT = Display.dp2px(20f)
        @JvmField
        val PADDING_BOTTOM = Display.dp2px(20f)
        const val MAX_FOLDER_INDEX = 12

        @JvmStatic
        fun getMinSize(context: Context, isDockItem: Boolean): Size {
            val paddingStart = context.resources.getDimensionPixelSize(R.dimen.app_padding_start)
            val paddingTop = context.resources.getDimensionPixelSize(R.dimen.app_padding_top)
            val paddingEnd = context.resources.getDimensionPixelSize(R.dimen.app_padding_end)
            val paddingBottom = context.resources.getDimensionPixelSize(R.dimen.app_padding_bottom)
            val iconWidth = context.resources.getDimensionPixelSize(R.dimen.app_icon_width)
            val iconHeight = context.resources.getDimensionPixelSize(R.dimen.app_icon_height)
            val marginTop = context.resources.getDimensionPixelSize(R.dimen.app_margin_label_top)
            val labelSize = context.resources.getDimensionPixelSize(R.dimen.app_text_size)
            val labelHeight = Display.sp2px(labelSize.toFloat())
            val width = paddingStart + iconWidth + paddingEnd
            var height = paddingTop + iconHeight + marginTop + labelHeight + paddingBottom
            if (isDockItem) {
                height -= labelHeight
            }
            return Size(width, height)
        }
    }

    enum class Type {
        APP, FOLDER, SHORTCUT, WIDGET // ,
        // EMPTY
    }

    enum class Location {
        MENU, FOLDER, DESKTOP, DOCK
    }

    // item
    private var mPageCurrentStored: Any? = null
    var pageCurrentEvent: Any? = null
        private set
    var parentPosition: Point? = null
    var childPosition: Point? = null
    var parentSize: Size? = null
    var childSize: Size? = null
    abstract fun setItemView()
    abstract fun updateView()
    abstract val currentPage: Any?
    abstract val pageIndex: Int

    // app specifications
    var bitmapIcon: Bitmap? = null
        private set
    var drawableIcon: FolderDrawable? = null
        private set
    open var label = ""
    var type: Type? = null
    var location: Location? = null
        private set

    /*
    *
    */  var ID = 0
    var iconWidth = 0
        private set
    var iconHeight = 0
        private set
    var labelColor: Int
    var labelVisible = true

    // folder
    var folderApps : ArrayList<App>? = null

    // app
    var app: App? = null

    protected constructor(context: Context) : super(context) {
        init()
        labelColor = Color.WHITE
    }

    protected constructor(desktopFragment: DesktopFragment, cursor: Cursor, layout: Any?) : super(desktopFragment.requireContext()) {
        val columnLocation = cursor.getString(cursor.getColumnIndex(DBItemProfile.Table.COLUMN_LOCATION))
        val columnType = cursor.getString(cursor.getColumnIndex(DBItemProfile.Table.COLUMN_TYPE))
        val columnID = cursor.getString(cursor.getColumnIndex(DBItemProfile.Table.COLUMN_TIME))
        val columnParentX = cursor.getString(cursor.getColumnIndex(DBItemProfile.Table.COLUMN_PARENT_X))
        val columnParentY = cursor.getString(cursor.getColumnIndex(DBItemProfile.Table.COLUMN_PARENT_Y))
        val columnChildX = cursor.getString(cursor.getColumnIndex(DBItemProfile.Table.COLUMN_CHILD_X))
        val columnChildY = cursor.getString(cursor.getColumnIndex(DBItemProfile.Table.COLUMN_CHILD_Y))
        val columnParentWidth = cursor.getString(cursor.getColumnIndex(DBItemProfile.Table.COLUMN_PARENT_WIDTH))
        val columnParentHeight = cursor.getString(cursor.getColumnIndex(DBItemProfile.Table.COLUMN_PARENT_HEIGHT))
        val columnChildWidth = cursor.getString(cursor.getColumnIndex(DBItemProfile.Table.COLUMN_CHILD_WIDTH))
        val columnChildHeight = cursor.getString(cursor.getColumnIndex(DBItemProfile.Table.COLUMN_CHILD_HEIGHT))
        val label = cursor.getString(cursor.getColumnIndex(DBItemProfile.Table.COLUMN_LABEL))
        val type = Type.valueOf(columnType)
        val location = Location.valueOf(columnLocation)
        val id = columnID.toInt()
        val parentPoint = Point(columnParentX.toInt(), columnParentY.toInt())
        val childPoint = Point(columnChildX.toInt(), columnChildY.toInt())
        val parentSize = Size(columnParentWidth.toInt(), columnParentHeight.toInt())
        val childSize = Size(columnChildWidth.toInt(), columnChildHeight.toInt())
        setPageCurrentEvent(layout, location)
        this.type = type
        ID = id
        this.label = label
        labelColor = if (location == Location.FOLDER) Color.BLACK else Color.WHITE
        labelVisible = location != Location.DOCK
        parentPosition = parentPoint
        childPosition = childPoint
        this.parentSize = parentSize
        this.childSize = childSize
        init()
    }

    private fun init() {
//        mPositionParent = new Point(0, 0);
//        mPositionChild = new Point(0, 0);
//        mSizeParent = new Size(1, 1);
//        mSizeChild = getCellPxSize();
        iconWidth = this.resources.getDimensionPixelSize(R.dimen.app_icon_width)
        iconHeight = this.resources.getDimensionPixelSize(R.dimen.app_icon_height)
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    override fun equals(obj: Any?): Boolean {
        return if (obj is ItemAppView) {
            ID == obj.ID
        } else if (obj is Item) {
            ID == obj.ID
        } else {
            false
        }
    }

    fun setParentSize(width: Int, height: Int) {
        parentSize = Size(width, height)
    }

    val parentBorder: Rect
        get() {
            val border = Rect()
            border.left = parentPosition!!.x
            border.top = parentPosition!!.y
            border.right = parentPosition!!.x + parentSize!!.width
            border.bottom = parentPosition!!.y + parentSize!!.height
            return border
        }

//    fun setChildPosition(x: Int, y: Int) {
//        childPosition = Point(x, y)
//    }

    val childBorder: Rect
        get() {
            val border = Rect()
            border.left = childPosition!!.x
            border.top = childPosition!!.y
            border.right = childPosition!!.x + childSize!!.width
            border.bottom = childPosition!!.y + childSize!!.height
            return border
        }

    fun setPageCurrentStored(pageObject: Any?, location: Location?) {
        mPageCurrentStored = pageObject
        pageCurrentEvent = mPageCurrentStored
        this.location = location
    }

    fun setPageCurrentEvent(page: Any?) {
        location = when (page) {
            is DockView -> {
                Item.Location.DOCK
            }
            is CellContainer -> {
                Item.Location.DESKTOP
            }
            is LinearLayout -> {
                Item.Location.FOLDER
            }
            else -> Item.Location.DESKTOP
        }

        setPageCurrentEvent(page, location)
    }

    fun setPageCurrentEvent(pageObject: Any?, location: Location?) {
        pageCurrentEvent = pageObject
        setPageCurrentStored(pageCurrentEvent, location)
    }

    open fun setBitmapIcon(icon: Bitmap) {
        bitmapIcon = Tool.resizeBitmap(icon, iconWidth, iconHeight)
    }

    fun setDrawableIcon(folder: FolderDrawable) {
        bitmapIcon = Tool.resizeBitmap(folder.icon, iconWidth, iconHeight)
        drawableIcon = folder
    }

    fun removeItemView() {
        // Desktop & Dock View
        if (mPageCurrentStored is CellContainer) {
            (mPageCurrentStored as CellContainer).removeItem(this)
        } else if (mPageCurrentStored is RevealLinearLayout) {
            (mPageCurrentStored as RevealLinearLayout).removeView(this)
        } else if (mPageCurrentStored is ViewGroup) {
            (mPageCurrentStored as ViewGroup).removeView(this)
        }
    }

    fun removeFolderItem(target: App?) {
        if (folderApps != null && folderApps!!.isNotEmpty()) {
            folderApps!!.remove(target)
        }
    }

    /**
     * threshold when you drag the
     * item than ignore the action untill
     * he pass the threshold border
     */
    fun isOutsideThreshold(finger: Point?, startPosition: Point?): Boolean {
        return if (finger == null || startPosition == null) {
            false
        } else {
            val left    = (startPosition.x - DRAG_THRESHOLD)
            val top     = (startPosition.y - DRAG_THRESHOLD)
            val right   = (startPosition.x + DRAG_THRESHOLD)
            val bottom  = (startPosition.y + DRAG_THRESHOLD)

            ((finger.x in left..right) && (finger.y in top..bottom))
        }
    }

    fun setOutsideThreshold() {
        // remove current view
        removeItemView()
        setPageCurrentEvent(currentPage, location)
    }

    fun onBorderOccupiedGuard() {
        val page = pageCurrentEvent as CellContainer?
        val border = parentBorder
        val size = parentSize
        val cellItem = page!!.getItem(border)

        // occupied security
        if (cellItem != null && cellItem.type == Type.WIDGET) {
            if (!page.isEmptySpan(border)) {
                val point = size?.let { page.getEmptySpan(it) }
                parentPosition = point
            }
        }
        page.onRemoveBorderParent()
    }// 1 cell size

    /**
     * all for cell and pixel
     * calculations based on his current
     * CellContainer (located page)
     */
    val cellPxSize: Size?
        get() = when {
            type == Type.WIDGET -> {
                val size = parentSize
                val cellSize = (pageCurrentEvent as CellContainer?)!!.cellSize
                Size(
                        cellSize.width * size!!.width,
                        cellSize.height * size.height
                )
            }
            pageCurrentEvent !is CellContainer -> {
                childSize
            }
            else -> {
                // 1 cell size
                (pageCurrentEvent as CellContainer).cellSize
            }
        }// more pages on the Y-axis, get Y of dock
    // Menu & Folder


    fun getParentFinger(finger: Point, page : CellContainer) : Point {
        val borderPosition = Point(
                (parentPosition!!.x * page.cellWidth),
                (parentPosition!!.y * page.cellHeight)
        )

        return Point(finger.x - borderPosition.x, finger.y - borderPosition.y
        )
    }

    fun getChildFinger(parentFinger : Point): Point {
        return Point(
                parentFinger.x - childPosition!!.x,
                parentFinger.y - childPosition!!.y
        )
    }


    // Dock
    val childParentPosition: Point
        get() {
            // Menu & Folder
            if (parentPosition!!.x == -1 && parentPosition!!.y == -1) {
                return Point( this.width / 2, this.height / 2)
            }
            var itemParentX = 0
            var itemParentY = 0
            val page = pageCurrentEvent as CellContainer

            // Dock
            if (location == Location.DOCK) {
                val paginationHeight = this.resources.getDimensionPixelSize(R.dimen.page_indicator_height)

                // more pages on the Y-axis, get Y of dock
                itemParentY += page.y.toInt() - paginationHeight
            }
            itemParentX += parentPosition!!.x * page.cellWidth
            itemParentY += parentPosition!!.y * page.cellHeight
            return Point(itemParentX, itemParentY)
        }

    fun getCellCeilX(px: Int): Int {
        val page = pageCurrentEvent as CellContainer?
        return Math.ceil(px / page!!.cellWidth.toDouble()).toInt()
    }

    fun getCellCeilY(px: Int): Int {
        val page = pageCurrentEvent as CellContainer?
        return Math.ceil(px / page!!.cellHeight.toDouble()).toInt()
    }

    fun getPixelX(cell: Int): Int {
        val page = pageCurrentEvent as CellContainer?
        return cell * page!!.cellWidth
    }

    fun getPixelY(cell: Int): Int {
        val page = pageCurrentEvent as CellContainer?
        return cell * page!!.cellHeight
    }

}