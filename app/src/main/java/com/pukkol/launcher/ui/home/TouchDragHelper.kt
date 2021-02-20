package com.pukkol.launcher.ui.home

import android.graphics.*
import android.util.Log
import android.view.*
import android.view.MotionEvent.*
import com.pukkol.launcher.data.model.Item
import com.pukkol.launcher.ui.home.TouchDragHelper.DragMode.*
import com.pukkol.launcher.ui.home.FragmentSwitcher.Companion.switchedState
import com.pukkol.launcher.ui.home.FragmentSwitcher.SwitchedState.IN_EDIT_DESKTOP
import com.pukkol.launcher.ui.home.desktop.*
import com.pukkol.launcher.ui.itemview.ItemAppView
import com.pukkol.launcher.ui.itemview.ItemViewManager
import com.pukkol.launcher.util.Display
import java.util.*


// TO DO make abstract

/**
 * @author okido
 * @since 1/22/2021
 * interact with all Drag action on the Desktop
 */
@Suppress("DEPRECATION")
class TouchDragHelper(private val mTouchView: HomeTouchView) {
    enum class DragMode {
        DRAG_ITEM_MODE, DRAG_MENU_MODE, OPTION_MODE, DEFAULT_MODE
    }

    var dragMode = DEFAULT_MODE
        private set

    private lateinit var switcher: FragmentSwitcher

    lateinit var folderPreview: FolderPreviewView
    private lateinit var homeOptions: HomeOptionsView
    private lateinit var homeFingerItem: HomeFingerView

    private var mItem : Item? = null
    private var isInsideParent = false
    private var finger = Point(-1, -1)
    get() {
        return field
    }
    set(value) {
        field = value
    }
    private var startingPoint = Point(0, 0)
    private var isDraggingLayout = false

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

    val currentPage: Any?
        get() {
            val desktop = switcher.desktopFragment.pagerView!!.currentPage
            val dock = switcher.desktopFragment.dockView
            var page: Any? = null
            if (dock == null || desktop == null) return null
            if (isInsideView(desktop)) {
                page = desktop
            } else if (isInsideView(dock)) {
                page = dock
            }
            return page
        }

    val currentPageIndex: Int
        get() {
            val desktop = switcher.desktopFragment.pagerView!!.currentPage ?: return 0
            return if (isInsideView(desktop)) {
                switcher.desktopFragment.pagerView!!.currentItem
            } else {
                0
            }
        }

    private val pages: List<CellContainer>?
        get() {
            val desktop = switcher.desktopFragment.pagerView!!.currentPage
            val dock = switcher.desktopFragment.dockView
            if (dock == null || desktop == null) return null

            val insideDesktop = isInsideView(desktop)
            val insideDock = isInsideView(dock)
            return when {
                insideDesktop -> {
                    switcher.desktopFragment.pagerView!!.pages
                }
                insideDock -> {
                    ArrayList(listOf(dock))
                }
                else -> {
                    null
                }
            }
        }






    fun withDesktopFragment(desktopFragment: DesktopFragment) {
        val context = mTouchView.context
        switcher = desktopFragment.fragmentSwitcher

        folderPreview = FolderPreviewView(context)
        mTouchView.addView(folderPreview)
        homeOptions = HomeOptionsView(mTouchView)
        mTouchView.addView(homeOptions)
        homeFingerItem = HomeFingerView(context)
        mTouchView.addView(homeFingerItem)

         // testing
         folderPreview.setBackgroundColor(Color.parseColor("#10000000"))
         homeFingerItem.setBackgroundColor(Color.parseColor("#35FFFFFF"))
         homeOptions.setBackgroundColor(Color.parseColor("#F1343434"))
    }

    fun onBackPressed() {
        if (dragMode != DEFAULT_MODE) {
            dragMode = DEFAULT_MODE
            homeOptions.onHideView()
        }
    }

//    fun onInterceptTouchEvent(event: MotionEvent): Boolean {
//        // TO DO testing
//        Log.d("onInterceptTouchEvent", "onTouch: dragMode = ${dragMode}")
//        ///////////////
//
//        updateFinger(event)
//
//        when (dragMode) {
//            OPTION_MODE -> {
//                return true// !homeOptions.inBorder(finger)
////                if(item != null) {
////                    if(!isOutsideThreshold()) {
////                        val inBorder = homeOptions.inBorder(finger)
////                        if(event.action == ACTION_DOWN && !inBorder) {
//////                            dragMode = DEFAULT_MODE
////                            homeOptions.onHideView()
//////                            onBackPressed()
////                        }
////
////                        // return false
////                        // return !inBorder
////                    }
////
////                    return true
////                }
//            }
//            DRAG_ITEM_MODE -> {
//                if(event.action == ACTION_DOWN) {
//                    isInsideParent = homeFingerItem.isInsideParent(finger)
//
//                    if (isInsideParent) {
//                        homeFingerItem.onDrop(this)
//                    } else {
//                        onItemDragStop()
//                    }
//                }
//
//                return isInsideParent
//            }
//            DRAG_MENU_MODE, DEFAULT_MODE -> {
//                return false
//            }
//        }
//    }

    fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        // TO DO testing
        Log.d("onTouch", "onTouch: dragMode = $dragMode")
        ///////////////

        updateFinger(motionEvent)

        // disable touch
        if(switchedState == IN_EDIT_DESKTOP){
            return false
        }

        return when(dragMode) {
            DRAG_ITEM_MODE, OPTION_MODE -> {
                onTouchItem(view, motionEvent)
            }
            DRAG_MENU_MODE, DEFAULT_MODE -> {
                onTouchLayout(view, motionEvent)
            }
        }
    }

    private fun onTouchItem(view: View, event: MotionEvent?): Boolean {
        if(item == null) return true

        var onItemView = true
//        if(view is Item) {
//            // from Item onTouch listener
//        Log.d("TouchDragHelper", "onTouchItem: from Item onTouch listener")
//            return false
//        }


        when (event!!.action) {
            ACTION_MOVE -> {
                return onItemDrag()
            }
            ACTION_UP -> {
                // item events
                onItemView = homeFingerItem.isInsideParent(finger)
                if (onItemView) {
                    homeFingerItem.onDrop(finger, this)
                } else {
                    onItemDragStop()
                }
            }
        }

        return onItemView
    }

    private fun onTouchLayout(view : View, motionEvent: MotionEvent) : Boolean {
        // drag Layout
        when (motionEvent.action) {
            ACTION_MOVE -> {
                val movement = PointF(
                        (finger.x - startingPoint.x).toFloat(),
                        (finger.y - startingPoint.y).toFloat()
                )

                val openMenu = intentionForMenu(movement)
                if (openMenu) {
                    dragMode = DRAG_MENU_MODE
                    return switcher.menuFragment.pageLayout.onMotionMenu(view, motionEvent, startingPoint.y)
                }

                // return true
            }
            ACTION_UP,  ACTION_CANCEL -> {
                if (dragMode == DRAG_MENU_MODE) {
                    isDraggingLayout = false
                    dragMode = DEFAULT_MODE
                    switcher.menuFragment.pageLayout.onMotionMenu(view, motionEvent, startingPoint.y)
                }
            }
        }

        return false
    }













    fun onItemDragStart(value: Item) {
        item = value
        dragMode = OPTION_MODE
        homeOptions.onShowView(item, finger)
    }



    private fun onItemDrag() : Boolean {
        val ret = true
        if(!isOutsideThreshold()) {
            return ret
        }

        // draw item
        val page = homeFingerItem.onDrag(finger)
        val pagination = switcher.desktopFragment.pagerView!!.dragFields

        if (page is CellContainer) {
            pagination.onInteract(finger, page)
            pagination.setVisibleState(pages!!, currentPageIndex)
            page.onCreateBorder(homeFingerItem, page, folderPreview)
        }


        return ret
    }

    fun onItemDragStop() {
        if (dragMode == DRAG_ITEM_MODE) {
            val fingerItem = homeFingerItem.onStop()
            val pagination = switcher.desktopFragment.pagerView!!.dragFields

            pagination.onDestroyView()
            ItemViewManager.setInDesktop(switcher.desktopFragment, fingerItem!!)
            switcher.desktopFragment.pagerView!!.onRemoveEmptyPages()
        }

        // mInDragState = false;
        dragMode = DEFAULT_MODE
        folderPreview.cancel(true)
    }

    fun setDragState(dragMode: DragMode) {
        this.dragMode = dragMode
    }


    private fun isOutsideThreshold() : Boolean {
        // threshold on item
        if (dragMode != DRAG_ITEM_MODE) {
            // 1 time call
            val isOutsideThreshold = item!!.isOutsideThreshold(finger, startingPoint)
            if (isOutsideThreshold) {
                item!!.setOutsideThreshold()

                // specific changes
                if (item!!.location === Item.Location.MENU) {
                    (item as ItemAppView).resetID()
                    switcher.motionToDesktop()
                } else if (item!!.location === Item.Location.FOLDER) {
                    switcher.desktopFragment.folderView!!.onRemoveItem(item as ItemAppView)
                }

                // start drag event
                dragMode = DRAG_ITEM_MODE// update drag state
                homeOptions.onHideView()
                homeFingerItem.item = item
            }
        }

        return (dragMode == DRAG_ITEM_MODE)
    }

    private fun isInsideView(view: View): Boolean {
        val viewPosition = IntArray(2)
        view.getLocationOnScreen(viewPosition)

        val left    = (viewPosition[0])
        val top     = (viewPosition[1])
        val right   = (viewPosition[0] + view.width)
        val bottom  = (viewPosition[1] + view.height)
        return ((finger.x in left..right) && (finger.y in top..bottom))
    }

    private fun intentionForMenu(movement: PointF): Boolean {
        if(isDraggingLayout) return true
        val minDragX = Display.DISPLAY_SIZE!!.width * 0.05 // 5%
        val minDragY = Display.DISPLAY_SIZE!!.height * 0.05 // 5%

        val inLineX = (movement.x > -minDragX || movement.x < minDragX)
        val inLineOpenMenu = (dragMode == DEFAULT_MODE && movement.y < -minDragY)
        val inLineCloseMenu = (dragMode == DRAG_MENU_MODE && movement.y > minDragY)

        isDraggingLayout = (inLineX && (inLineOpenMenu || inLineCloseMenu))
        return isDraggingLayout
    }

    private fun updateFinger(event: MotionEvent) {
        finger = Point(event.rawX.toInt(), event.rawY.toInt())

        if (event.action == ACTION_DOWN) {
            startingPoint = Point(finger.x, finger.y)
        }
    }

}