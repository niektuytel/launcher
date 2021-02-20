package com.pukkol.launcher.ui.home

import android.content.Context
import android.graphics.*
import android.util.Size
import android.view.View
import com.pukkol.launcher.data.model.Item
import com.pukkol.launcher.ui.home.desktop.CellContainer
import com.pukkol.launcher.util.Display
import com.pukkol.launcher.util.Tool

/**
 * @author okido 2021(2/10/2021)[Niek Tuytel]
 * <>
 * // required
 * --------------------------------------
 *
 * update location finger item:
 * mFingerItem.onDrag(Point);\
 *
 * drop location finger item:
 * mFingerItem.onDrop(DragHelper);
 *
 * remove finger item:
 * mFingerItem.onStop();
 *
 * -------------------------------------
 *
 * check finger inside Parent of item:
 * mFingerItem.isInsideParent(mFinger);
 *
 * set finger item:
 * mFingerItem.setItem(ItemAppView);
 *
 * get finger item:
 * mFingerItem.getItem();
 *
 * get item px position count from parent border:
 * mFingerItem.getPositionItem();
 *
 * >
 */
class HomeFingerView(context: Context?) : View(context) {
    private var mItem: Any? = null
    private var itemBitmap: Bitmap? = null
    private var startFingerLayout: Point? = null
    private var startFingerParent: Point? = null
    private var startFingerChild: Point? = null
//    private var startParentLayout: Point? = null
//    private var startChildParent: Point? = null
//    private var startChildSize: Size? = null
    var pointParentLayout = Point(0, 0)
//    var pointChildParent = Point(0, 0)

    private val statusBarHeight = Display.STATUSBAR_HEIGHT
    private val paint = Paint()

    init {
        true.also {
            paint.isAntiAlias = it
            paint.isFilterBitmap = it
            paint.isDither = it
        }

        this.y = statusBarHeight.toFloat()
    }

    var item: Item?
        get() {
            if(mItem == null) {
                try {
                    throw Exception("Not able to drag a null(mItem)")
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return null
            }

            return (mItem as Item)
        }
        set(item) {
            if(item == null) {
                try {
                    throw Exception("Not able to drag a null(mItem)")
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return
            }

            mItem = item
        }

//    val pointChild: Point
//        get() = Point(
//                pointParentLayout.x + startChildParent!!.x,
//                pointParentLayout.y + startChildParent!!.y
//        )
//
//    val sizeChild: Size
//        get() = startChildSize!!

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // item draw
        if(item != null) {
            canvas.drawBitmap(
                    itemBitmap!!,
                    pointParentLayout.x.toFloat(),
                    pointParentLayout.y.toFloat(),
                    paint
            )
        }
    }

    fun onDrag(finger: Point): Any {
        val currentPage = updatePage()
        val page = currentPage as CellContainer

        // update view item on finger
        item!!.updateView()
        itemBitmap = Tool.viewToBitmap(item!!)

        // one time call
        if (startFingerLayout == null) {
            startFingerLayout = finger
            startFingerParent = item!!.getFingerParent(finger, page)
            startFingerChild = item!!.getFingerChild(startFingerParent!!, item!!.childPosition!!)
        }

        item!!.childPosition = Point(
                item!!.getFingerParent(finger, page).x - startFingerChild!!.x,
                item!!.getFingerParent(finger, page).y - startFingerChild!!.y
        )

        pointParentLayout = Point(
                finger.x - startFingerParent!!.x,
                finger.y - startFingerParent!!.y
        )

        this.invalidate()
        return currentPage
    }

    fun onDrop(finger: Point, touchDragHelper: TouchDragHelper) {
//        // update child position
//        val currentPage = updatePage()
//        val page = currentPage as CellContainer
//        val fingerParent = item!!.getParentFinger(finger, page)
//        val childParent = Point(
//                fingerParent.x - pointFingerChild!!.x,
//                fingerParent.y - pointFingerChild!!.y
//        )
//
//        item!!.childPosition = childParent


        /*if (item!!.location === Item.Location.DOCK) {
            // on location dock
            touchDragHelper.onItemDragStop()
        } else {
            // on folder dragged
            val preview = touchDragHelper.folderPreview
            if (preview.isOnPreview) {
                touchDragHelper.onItemDragStop()
            }
        }*/

//
//        Log.d("childParent ", "onDrop: $childParent")
//
//        // occupied guardian check
//        pointChildParent = Point(0, 0)
//        pointFingerParent = Point(0, 0)
//        pointParent = Point(0, 0)

        // val currentPage = updatePage()
        // val page = currentPage as CellContainer
        // item!!.childPosition = pointChildParent
        // item!!.setChildPosition(finger, startFingerChild!!, page)

        startFingerLayout = null
        startFingerParent = null
        startFingerChild = null


        // this.invalidate()

        // pointParent = Point(0, 0)
        // pointItemParent = Point(0, 0)

        // add to Folder if on other item and stop drag
        // this.invalidate()




//
//        // update child location
//        val fingerParent = item.getParentFinger(finger, this)
//        val fingerChild = item.getChildFinger(fingerParent)
//        val childParent = Point(
//                fingerParent.x - fingerChild.x,
//                fingerParent.y - fingerChild.y
//        )
//        item.childPosition = childParent







    }

    fun onStop(): Any? {
        // occupied guardian check
        item!!.onBorderOccupiedGuard()
        val i = mItem
        mItem = null
        itemBitmap = null
        startFingerChild = null
        startFingerParent = null
        pointParentLayout = Point(0, 0)
//        pointChildParent = Point(0, 0)
        startFingerLayout = null

        this.invalidate()
        return i
    }

    fun isInsideParent(finger: Point): Boolean {
        return  (finger.x >= pointParentLayout.x && finger.x <= (pointParentLayout.x + item!!.width))// X
                    &&
                (finger.y >= pointParentLayout.y && finger.y <= (pointParentLayout.y + item!!.height))// Y
    }

    private fun updatePage(): Any? {
        val newPage = item!!.currentPage
        val oldPage = item!!.pageCurrentEvent
        item!!.setPageCurrentEvent(newPage)

        // remove old border drawment
        if (oldPage != null && newPage != oldPage) {
            (oldPage as CellContainer).onRemoveBorderParent()
        }

        return newPage
    }


//    private fun getParentFinger(finger: Point, page : CellContainer, item: Item) : Point {
//        val itemSize = Size(
//                (item.parentSize!!.width * page.cellWidth),
//                (item.parentSize!!.height * page.cellHeight)
//        )
//
//        return Point(
//                finger.x % itemSize.height,
//                finger.y % itemSize.width
//        )
//    }

//    private fun getChildFinger(parentFinger : Point): Point {
//        return Point(
//                parentFinger.x -  item!!.childPosition!!.x,
//                parentFinger.y -  item!!.childPosition!!.y
//        )
//
//        // first time call
////        var childPoint = Point(0, 0)
////        if (pointParent.y == 0 && pointParent.x == 0) {
////            childPoint = item!!.childParentPosition
////        }
////
////        return Point(
////                finger.x - pointParent.x,
////                finger.y - pointParent.y
////        )
//    }
}