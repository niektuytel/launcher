package com.pukkol.launcher.ui.home.menu

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pukkol.launcher.R
import com.pukkol.launcher.Setup
import com.pukkol.launcher.data.model.App
import com.pukkol.launcher.data.model.Item
import com.pukkol.launcher.interfaces.AppUpdateListener
import com.pukkol.launcher.ui.home.desktop.DesktopFragment
import com.pukkol.launcher.ui.home.menu.MenuSearchView.OnSearchCallback
import com.pukkol.launcher.ui.itemview.GridAdapter
import com.pukkol.launcher.util.Tool
import kotlinx.android.synthetic.main.view_menu_scroll.view.*
import me.everything.android.ui.overscroll.IOverScrollDecor
import me.everything.android.ui.overscroll.IOverScrollState
import me.everything.android.ui.overscroll.IOverScrollUpdateListener
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper.ORIENTATION_VERTICAL
import kotlin.math.abs

class MenuPageView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyle: Int = 0)
    :
        LinearLayout(context, attrs, defStyle),
        View.OnTouchListener,
        IOverScrollUpdateListener,
        OnSearchCallback
{
    private val mRecyclerView: RecyclerView
    private lateinit var mAdapter: GridAdapter
    private var mMenuGridMode: MenuGridMode
    private var mCallback: OnMenuPageCallback? = null
    private var mDesktopFragment: DesktopFragment? = null
    private var mStartY = 0
    private var mBeenCalled = false

    enum class MenuGridMode {
        ON_SCROLL_MODE, ON_DEFAULT_MODE
    }

    init {
        LayoutInflater.from(context)
                .inflate(R.layout.view_menu_scroll, this, true)

        val gridLayoutManager = GridLayoutManager(context, Setup.deviceSettings().cellHorizontalAmount)
        gridLayoutManager.orientation = RecyclerView.VERTICAL
        mMenuGridMode = MenuGridMode.ON_DEFAULT_MODE

        mRecyclerView = recycler_view_menu.also {
            it.layoutManager = gridLayoutManager

            OverScrollDecoratorHelper.setUpOverScroll(it, ORIENTATION_VERTICAL)
                    .setOverScrollUpdateListener(this)
        }
    }

    /*
    * update the list of result with a new
    * list of items from te default loaded applications
    * and set it to the recyclerView
     */
    override fun onQuerySearch(query: String?) {
        if (query!!.isEmpty()) {
            // default results
            mAdapter.updateData(Setup.appManager().getApps(false))
        } else {
            val filteredApps: ArrayList<App> = Tool.searchAppsFilter(
                    Setup.appManager().getApps(false),
                    query
            )
            mAdapter.updateData(filteredApps)
        }
    }

    /*
    * functions called below until first following Comment
    * is needed to catch the finger
    * on dragging the Menu Fragment | on scrolling the items RecyclerView
    */
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        val res = onMotionMenu(mRecyclerView, event, mStartY)
        return res || super.onTouchEvent(event)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return onMotionMenu(mRecyclerView, event, mStartY)
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        return onMotionMenu(view, motionEvent, mStartY)
    }

    override fun onOverScrollUpdate(decor: IOverScrollDecor, state: Int, offset: Float) {
        if (IOverScrollState.STATE_BOUNCE_BACK == state && offset != 0f) {
            mMenuGridMode = if (offset > 0) {
                // 'view' is currently being over-scrolled from the top.
                MenuGridMode.ON_DEFAULT_MODE
            } else {
                // No over-scroll is in-effect.
                MenuGridMode.ON_SCROLL_MODE
            }
        }
    }

    fun initView(desktopFragment: DesktopFragment?, callback: OnMenuPageCallback?) {
        mMenuGridMode = MenuGridMode.ON_DEFAULT_MODE
        mDesktopFragment = desktopFragment
        mCallback = callback

        // create adapter with items
        Setup.appManager().addUpdateListener(
                object : AppUpdateListener {
                    override fun onAppUpdated(apps: ArrayList<App>): Boolean {
                        mAdapter.updateData(apps)
                        return false
                    }
                }
        )

        mAdapter = GridAdapter(Item.Location.MENU, mDesktopFragment!!)
        mRecyclerView.adapter = mAdapter
    }

    fun setAsDefault() {
        onQuerySearch("")
    }

    fun onMotionMenu(view: View, event: MotionEvent, startY: Int): Boolean {
        // get for sure
        if (event.action == MotionEvent.ACTION_DOWN) {
            mStartY = event.rawY.toInt()
            mBeenCalled = false
            return false
        } else {
            mStartY = startY
        }

        // not on RecyclerView
        if (view.id != mRecyclerView.id) {
            return mCallback!!.onFingerInteract(view, event, mStartY.toFloat())
        }

        // check if on top of recyclerview
        val dragY = mStartY - event.rawY.toInt()
        mMenuGridMode = if (!scrollOnTop() || dragY > 0) {
            MenuGridMode.ON_SCROLL_MODE
        } else {
            MenuGridMode.ON_DEFAULT_MODE
        }

        val onDragMenu = mMenuGridMode == MenuGridMode.ON_DEFAULT_MODE
        if (onDragMenu && scrollOnTop() || mBeenCalled) {
            if (abs(dragY) > 30) { // catch onLongClick or Drag
                return mCallback!!.onFingerInteract(view, event, mStartY.toFloat()).also { mBeenCalled = it }
            }
        }
        return false
    }

    /*
    * check if the recycler view inside the page is on the top of the recyclerView
    * if this is not the case let the finger interact with the scroll recyclerView
    * else scroll the Menu Fragment
    */
    private fun scrollOnTop(): Boolean {
        val layoutManager = (mRecyclerView.layoutManager as LinearLayoutManager?)!!
        return layoutManager.findFirstVisibleItemPosition() == 0
    }

    interface OnMenuPageCallback {
        fun onFingerInteract(view: View?, motionEvent: MotionEvent, startY: Float): Boolean
    }

}