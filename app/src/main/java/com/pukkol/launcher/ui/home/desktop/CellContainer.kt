package com.pukkol.launcher.ui.home.desktop

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Paint.Join
import android.util.AttributeSet
import android.util.Size
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import com.pukkol.launcher.Setup
import com.pukkol.launcher.data.model.Item
import com.pukkol.launcher.ui.home.HomeFingerView
import java.util.*
import kotlin.math.ceil

/**
 * @author okido (Niek Tuytel)[2/5/2021]
 * the CellContainer class is a ViewGroup that represents al cells of Views
 * and this cells are been used to set item Views inside
 */
@SuppressLint("ViewConstructor")
open class CellContainer(context: Context?, attr: AttributeSet?, params: LayoutParams?, rasterVertical: Int) : ViewGroup(context, attr) {
    private val mPaint = Paint(1)
    private val mBorderPaint = Paint(1)
    private val mBackgroundPaint = Paint(1)
    private var mBorderRect: Rect? = null
    private var mOccupied: Array<Array<Item?>>? = null
    var cellSpanH = 0
        private set
    var cellSpanV = 0
        private set
    var cellWidth = -1
        private set
    var cellHeight = -1
        private set
    var isBackgroundZoomed = false
        private set

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mOccupied == null) return

        // draw item
        for (i in 0 until childCount) {
            val parent = getChildAt(i)
            val parentParams : CellParams = parent.layoutParams as CellParams


            parent.measure(
                    MeasureSpec.makeMeasureSpec(parentParams.borderWidth * cellWidth, MeasureSpec.EXACTLY),//todo can as well width call than it is pixels
                    MeasureSpec.makeMeasureSpec(parentParams.borderHeight * cellHeight, MeasureSpec.EXACTLY)
            )
            val left = parentParams.borderX * cellWidth
            val top = parentParams.borderY * cellHeight
            val right = (parentParams.borderX + parentParams.borderWidth) * cellWidth
            val bottom = (parentParams.borderY + parentParams.borderHeight) * cellHeight
            parent.layout(left, top, right, bottom)
        }

        // draw background
        onDrawBackground(canvas)

        // draw preview border
        if (mBorderRect != null) {
            canvas.drawRect(mBorderRect!!, mBorderPaint)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (isBackgroundZoomed) true else super.onInterceptTouchEvent(ev)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}
    fun onRemoveBorderParent() {
        mBorderRect = null
        this.invalidate()
    }

    /*
     * creates a border from cell indexes
     * for example around the child item
     * that is visible for the user
     */
    fun onCreateBorder(homeFingerItem: HomeFingerView, page: CellContainer, folderPreview: FolderPreviewView?) {
        val item = homeFingerItem.item!!
        val childBorder = Rect()
        childBorder.left    = homeFingerItem.pointParentLayout.x + item.childPosition!!.x
        childBorder.top     = homeFingerItem.pointParentLayout.y + item.childPosition!!.y
        childBorder.right   = childBorder.left + item.childSize!!.width
        childBorder.bottom  = childBorder.top + item.childSize!!.height

        if (childBorder.width() != cellWidth || childBorder.height() != cellHeight) {
            // dynamic parentCell
            onDrawDynamicBorder(childBorder, item, page)
        } else {
            // static parentCell
            onDrawStaticBorder(childBorder, item, page, folderPreview!!)
        }
    }

//
//            // update child location
//            val fingerParent = item.getParentFinger(finger, this)
//            val fingerChild = item.getChildFinger(fingerParent)
//            val childParent = Point(
//                    fingerParent.x - fingerChild.x,
//                    fingerParent.y - fingerChild.y
//            )
//            item.childPosition = childParent
//
//            val parentX = item!!.parentPosition!!.x * cellWidth
//            val parentY = item.parentPosition!!.y * cellHeight
//            val parentW = item.parentSize!!.width * cellWidth
//            val parentH = item.parentSize!!.height * cellHeight
//            childX = childPX.left - parentX
//            childY = childPX.top - parentY
//            childW = item.childSize!!.width
//            childH = item.childSize!!.height
//
//            // update child border
//            if (childX + childW > parentW) {
//                childX = parentW - childW
//            } else if (childX < 0) {
//                childX = 0
//            }
//            if (childY + childH > parentH) {
//                childY = parentH - childH
//            } else if (childY < 0) {
//                childY = 0
//            }
//            item.setChildPosition(childX, childY)

//    fun getParentFinger(finger: Point) : Point {
//        return Point(
//                finger.x % cellWidth,
//                finger.y % cellHeight
//        )
//    }
//
//    fun getChildFinger(parentFinger : Point, item: Item): Point {
//        return Point(
//                parentFinger.x -  item.childPosition!!.x,
//                parentFinger.y -  item.childPosition!!.y
//        )
//    }



//    fun onDrawBorder(item: Item, page: CellContainer) {
//        mBorderRect = item.getParentBorder(page)
//
////        val position = item!!.parentPosition
////        val size = item.parentSize
////        mBorderRect = Rect(
////                position!!.x * cellWidth,
////                position.y * cellHeight,
////                (position.x + size!!.width) * cellWidth,
////                (position.y + size.height) * cellHeight
////        )
//
//
//        this.invalidate()
//    }

    fun onAnimateZoom(targetScale: Float) {
        isBackgroundZoomed = targetScale != 1.0f
        this.invalidate()
        val animation = animate()
                .scaleX(targetScale)
                .scaleY(targetScale)
        animation.interpolator = AccelerateDecelerateInterpolator()
    }

    fun setEmpty() {
        mOccupied = Array(cellSpanH) { arrayOfNulls<Item>(cellSpanV) }
        for (items in mOccupied!!) {
            Arrays.fill(items, null)
        }
    }

    fun isEmptySpan(border: Rect): Boolean {
        for (x in border.left until border.right) {
            for (y in border.top until border.bottom) {
                if (x < 0 || y < 0 || mOccupied!!.size <= x || mOccupied!![x].size <= y) {
                    return true
                } else if (mOccupied!![x][y] != null) {
                    return false
                }
            }
        }
        return true
    }

    val isEmpty: Boolean
        get() {
            for (occupiedGroup in mOccupied!!) {
                for (item in occupiedGroup) {
                    if (item != null) return false
                }
            }
            return true
        }

    fun getEmptySpan(span: Size): Point? {
        for (x in mOccupied!!.indices) {
            for (y in 0 until mOccupied!![x].size) {
                val border = Rect()
                border.left = x
                border.top = y
                border.right = x + span.width
                border.bottom = y + span.height
                if (isEmptySpan(border)) return Point(x, y)
            }
        }
        return null
    }

    fun setItem(item: Item) {
        setItem(item, item.parentBorder)
    }

    fun setItem(item: Item?, border: Rect) {
        item?.setItemView()
        if (border.left >= 0 && border.top >= 0) {
            for (x in border.left until border.right) {
                for (y in border.top until border.bottom) {
                    if (x < 0 || y < 0 || mOccupied!!.size <= x || mOccupied!![x].size <= y) {
                        return
                    } else {
                        mOccupied!![x][y] = item
                    }
                }
            }
        }
        if (item != null) {
            super.addView(item)
        }
    }

    fun removeItem(item: Item) {
        setItem(null, item.parentBorder)
        super.removeView(item)
        Setup.itemManager().delete(item)
        invalidate()
    }

    fun getItem(border: Rect): Item? {
        for (x in border.left until border.right) {
            for (y in border.top until border.bottom) {
                if (x < 0 || y < 0 || mOccupied!!.size <= x || mOccupied!![x].size <= y) {
                    return null
                } else if (mOccupied!![x][y] != null) {
                    return mOccupied!![x][y]
                }
            }
        }
        return null
    }

    val allItems: List<View>
        get() {
            val views = ArrayList<View>()
            for (i in 0 until childCount) {
                views.add(getChildAt(i))
            }
            return views
        }

    private fun onDrawDynamicBorder(childBorder: Rect, item: Item, page: CellContainer) {
        val layoutCell = Rect(0, 0, cellSpanH, cellSpanV)
        val parentCell = Rect()
        parentCell.left = childBorder.left / cellWidth
        parentCell.top = childBorder.top / cellHeight
        parentCell.right = ceil(childBorder.right.toDouble() / cellWidth.toDouble()).toInt()
        parentCell.bottom = ceil(childBorder.bottom.toDouble() / cellHeight.toDouble()).toInt()
        borderOverflowGuard(layoutCell, parentCell)

        // update parent border
        if (isEmptySpan(parentCell)) {
            item.setParentBorder(parentCell)// cell border
            mBorderRect = item.getParentBorder(page)// pixel border
            this.invalidate()
        }
    }

    private fun onDrawStaticBorder(childPX: Rect, item: Item, page: CellContainer, folderPreview: FolderPreviewView) {
        val parentW = item.parentSize!!.width
        val parentH = item.parentSize!!.height
        val childW = item.childSize!!.width
        val childH = item.childSize!!.height
        val centerWidth = childW / 2
        val centerHeight = childH / 2
        val layoutCell = Rect(0, 0, cellSpanH, cellSpanV)
        val parentCell = Rect()
        parentCell.left = (childPX.left + centerWidth) / cellWidth
        parentCell.top = (childPX.top + centerHeight) / cellHeight
        parentCell.right = parentCell.left + parentW
        parentCell.bottom = parentCell.top + parentH
        borderOverflowGuard(layoutCell, parentCell)
        var onCollide = false
        val childItem = getItem(parentCell)
        folderPreview.cancel(false)
        if (childItem != null) {
            // folder check
            val cellSize = if (childItem.folderApps == null) 0 else childItem.folderApps!!.size
            if (cellSize != 0 && item.type === Item.Type.FOLDER) {
                val itemSize = item.folderApps!!.size
                onCollide = cellSize + itemSize >= Item.MAX_FOLDER_INDEX
            }

            // widget check
            onCollide = onCollide || childItem.type === Item.Type.WIDGET
            if (!onCollide && !item.equals(childItem)) {
                val newLocation = Point(parentCell.left, parentCell.top)
                folderPreview.onPreview(this, newLocation)
            }
        }

        // update parent border
        if (!onCollide) {
            item.setParentBorder(parentCell)// cell border
            mBorderRect = item.getParentBorder(page)// pixel border
            this.invalidate()
        }
    }

//        val parentX = item!!.parentPosition!!.x * cellWidth
//        val parentY = item.parentPosition!!.y * cellHeight
//        val parentW = item.parentSize!!.width * cellWidth
//        val parentH = item.parentSize!!.height * cellHeight
//        var childX = childPX.left - parentX
//        var childY = childPX.top - parentY
//        val childW = item.childSize!!.width
//        val childH = item.childSize!!.height
//
//        // update child border
//        if (childX + childW > parentW) {
//            childX = parentW - childW
//        } else if (childX < 0) {
//            childX = 0
//        }
//        if (childY + childH > parentH) {
//            childY = parentH - childH
//        } else if (childY < 0) {
//            childY = 0
//        }
//        item.setChildPosition(childX, childY)

    private fun onDrawBackground(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), mBackgroundPaint)

        // Animating alpha
        if (mPaint.alpha != 0) {
            mPaint.alpha = Math.max(mPaint.alpha - 20, 0)
            this.invalidate()
        } else if (mPaint.alpha != 255) {
            mPaint.alpha = Math.min(mPaint.alpha + 20, 255)
            this.invalidate()
        }

        // Animating alpha
        if (!isBackgroundZoomed && mBackgroundPaint.alpha != 0) {
            mBackgroundPaint.alpha = Math.max(mBackgroundPaint.alpha - 10, 0)
            this.invalidate()
        } else if (isBackgroundZoomed && mBackgroundPaint.alpha != 100) {
            mBackgroundPaint.alpha = Math.min(mBackgroundPaint.alpha + 10, 100)
            this.invalidate()
        }
    }

    private fun setRasterSize(cellHeight: Int): Size {
        val cellRaster = Size(
                Setup.deviceSettings().cellHorizontalAmount,
                cellHeight
        )
        cellSpanH = Math.max(1, cellRaster.width)
        cellSpanV = Math.max(1, cellRaster.height)
        return cellRaster
    }

    val rasterSize: Size
        get() = Size(cellSpanH, cellSpanV)
    var cellSize: Size
        get() = Size(cellWidth, cellHeight)
        private set(cellRaster) {
            val layoutHeight = this.layoutParams.height
            val cellSize = Size(
                    Setup.deviceSettings().cellWidth, layoutHeight / cellRaster.height
            )
            cellWidth = cellSize.width
            cellHeight = cellSize.height
        }

    companion object {
        private val TAG = CellContainer::class.java.simpleName

        /*
     * <code>borderChild</code> data will change
     * so he fit always inside the parent border
     **/
        private fun borderOverflowGuard(borderParent: Rect, borderChild: Rect) {
            // horizontal
            if (borderParent.left > borderChild.left) {
                val difference = borderParent.left - borderChild.left
                borderChild.left += difference
                borderChild.right += difference
            } else if (borderChild.right > borderParent.right) {
                val difference = borderChild.right - borderParent.right
                borderChild.left -= difference
                borderChild.right -= difference
            }

            // vertical
            if (borderParent.top > borderChild.top) {
                val difference = borderParent.top - borderChild.top
                borderChild.top += difference
                borderChild.bottom += difference
            } else if (borderChild.bottom > borderParent.bottom) {
                val difference = borderChild.bottom - borderParent.bottom
                borderChild.top -= difference
                borderChild.bottom -= difference
            }
        }
    }

    init {
        this.layoutParams = params
        // this.setBackgroundColor(Color.BLUE); // testing
        cellSize = setRasterSize(rasterVertical)
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 2.0f
        mPaint.strokeJoin = Join.ROUND
        mPaint.color = Color.WHITE
        mPaint.alpha = 0
        mBackgroundPaint.style = Paint.Style.FILL
        mBackgroundPaint.color = Color.WHITE
        mBackgroundPaint.alpha = 0
        mBorderPaint.style = Paint.Style.STROKE
        mBorderPaint.strokeWidth = 2.0f
        mBorderPaint.color = Color.WHITE
        setEmpty()
        setWillNotDraw(false)
    }
}