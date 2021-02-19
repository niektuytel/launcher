package com.pukkol.launcher.ui.home.desktop

import android.animation.Animator
import android.content.Context
import android.graphics.*
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Size
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pukkol.launcher.R
import com.pukkol.launcher.Setup
import com.pukkol.launcher.data.model.Item
import com.pukkol.launcher.ui.itemview.GridAdapter
import com.pukkol.launcher.ui.itemview.ItemAppView
import com.pukkol.launcher.ui.itemview.ItemViewManager
import com.pukkol.launcher.util.Display
import com.pukkol.launcher.util.Tool
import io.codetail.animation.ViewAnimationUtils
import io.codetail.widget.RevealLinearLayout
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper

/**
 * @author okido
 * @version 0.0.1
 * 1/23/2021
 * <>
 * Set DesktopFragment in Class: [REQUIRED]
 * void init(DesktopFragment desktopFragment);
 *
 * Open Folder:
 * boolean onFolderOpen(ItemAppView item)
 *
 * Close Folder:
 * boolean onMotionFolderClose(ItemAppView item)
 * >
 */
class FolderView(context: Context, attrs: AttributeSet?) : RevealLinearLayout(context, attrs), View.OnClickListener, TextWatcher {
    private var mItem: ItemAppView? = null
    private var mAnimator: Animator? = null
    private var mDesktopFragment: DesktopFragment? = null
    private val mGridLayoutManager = GridLayoutManager(context, AMOUNT_APPS_HORIZONTAL)
    private lateinit var mLayoutView: LinearLayout
    private lateinit var mTitleText: EditText
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: GridAdapter
    private val mStatusBarHeight: Int
    private val mNavBarHeight: Int
    private var mBeginLayoutSize: Size? = null
    private var mFolderLocation: Point? = null

    init {
        mStatusBarHeight = Display.STATUSBAR_HEIGHT
        mNavBarHeight = Display.NAVBAR_HEIGHT
    }


    fun init(desktopFragment: DesktopFragment?) {
        // not null
        if (desktopFragment == null) {
            throw NullPointerException(this.javaClass.simpleName + ": DesktopFragment == NULL, While this value is required")
        } else if (mDesktopFragment == null) {
            mDesktopFragment = desktopFragment
        }

        mAdapter = GridAdapter(Item.Location.FOLDER, mDesktopFragment!!)
        setOnClickListener(this)
        mLayoutView = LayoutInflater.from(context)
                .inflate(R.layout.view_folder, this, false) as LinearLayout
        mTitleText = mLayoutView.findViewById(R.id.text_folder_label)
        mRecyclerView = mLayoutView.findViewById(R.id.recycler_view_folder)

        mGridLayoutManager.orientation = RecyclerView.VERTICAL
        mRecyclerView.adapter = mAdapter
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = mGridLayoutManager
        OverScrollDecoratorHelper.setUpOverScroll(
                mRecyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL
        )
        this.addView(mLayoutView)
    }

    fun onFolderOpen(item: ItemAppView) {
        init(mDesktopFragment) // required

        // data
        mItem = item
        val folderItems = item.folderApps
        if (folderItems == null || folderItems.size == 0 || this.visibility == VISIBLE) {
            return
        }
        mAdapter.updateData(folderItems)
        if (mBeginLayoutSize == null) {
            mBeginLayoutSize = Size(mLayoutView.width, mLayoutView.height)
        }

        // folder
        val folderSize = folderItems.size
        val amountAppsHorizontal = Math.min(AMOUNT_APPS_HORIZONTAL, folderSize)
        val vertSize = Math.ceil(folderSize.toDouble() / AMOUNT_APPS_HORIZONTAL.toDouble()).toInt()
        mGridLayoutManager.spanCount = amountAppsHorizontal
        mRecyclerView.layoutParams.width = mItem!!.width * amountAppsHorizontal
        val listWidth = mRecyclerView.layoutParams.width
        mRecyclerView.layoutParams.height = mItem!!.height * Math.max(vertSize, 1)
        val listHeight = mRecyclerView.layoutParams.height
        mLayoutView.layoutParams.width = mBeginLayoutSize!!.width + listWidth
        val folderWidth = mLayoutView.layoutParams.width
        mLayoutView.layoutParams.height = mBeginLayoutSize!!.height + listHeight
        val folderHeight = mLayoutView.layoutParams.height
        val location = IntArray(2)
        item.getLocationInWindow(location)
        if (location[0] + folderWidth > width) {
            location[0] = width - folderWidth
        } else {
            location[0] -= folderWidth / 2 - mItem!!.width / 2
        }
        location[0] = Math.max(0, location[0])
        if (location[1] + folderHeight > height) {
            location[1] = height - folderHeight - mNavBarHeight
        } else {
            location[1] -= folderHeight / 2 - mItem!!.width / 2
        }
        location[1] = Math.max(mStatusBarHeight, location[1])
        mLayoutView.x = location[0].toFloat()
        mLayoutView.y = location[1].toFloat()
        mFolderLocation = Point(location[0], location[1])
        onMotionFolderOpen()
    }

    fun onRemoveItem(fingerItem: ItemAppView?) {
        ItemViewManager.setOutFolder(mDesktopFragment, mItem, fingerItem!!)
        onMotionFolderClose()
    }

    fun onMotionFolderClose() {
        if (this.visibility == INVISIBLE || mAnimator!!.isRunning) {
            return
        }
        val finalRadius = Math.max(mLayoutView.width, mLayoutView.height)
        val startRadius = (mItem!!.iconWidth / 2.00f / 2.00f).toInt()
        mAnimator = ViewAnimationUtils.createCircularReveal(mLayoutView, mFolderLocation!!.x, mFolderLocation!!.y, finalRadius.toFloat(), startRadius.toFloat())
        mAnimator!!.setStartDelay(1 + ANIMATION_DURATION / 2)
        mAnimator!!.setInterpolator(AccelerateDecelerateInterpolator())
        mAnimator!!.setDuration(ANIMATION_DURATION)
        mAnimator!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p1: Animator) {}
            override fun onAnimationEnd(p1: Animator) {
                mLayoutView.visibility = INVISIBLE
                visibility = INVISIBLE
                mItem!!.visibility = VISIBLE
                mItem!!.drawableIcon!!.popBack()
            }

            override fun onAnimationCancel(p1: Animator) {}
            override fun onAnimationRepeat(p1: Animator) {}
        })
        mAnimator!!.start()
        Tool.invisibleViews(ANIMATION_DURATION, mLayoutView)
    }

    private fun onMotionFolderOpen() {
        mLayoutView.alpha = 0f
        mLayoutView.visibility = VISIBLE
        visibility = VISIBLE
        val finalRadius = Math.max(mLayoutView.width, mLayoutView.height)
        val startRadius = (mItem!!.iconWidth / 2.00f / 2.00f).toInt()
        mAnimator = ViewAnimationUtils.createCircularReveal(mLayoutView, mFolderLocation!!.x, mFolderLocation!!.y, startRadius.toFloat(), finalRadius.toFloat())
        mAnimator!!.setStartDelay(0)
        mAnimator!!.setInterpolator(AccelerateDecelerateInterpolator())
        mAnimator!!.setDuration(ANIMATION_DURATION)
        mAnimator!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p1: Animator) {}
            override fun onAnimationEnd(p1: Animator) {
                mItem!!.visibility = GONE
                mItem!!.drawableIcon!!.popUp()
            }

            override fun onAnimationCancel(p1: Animator) {}
            override fun onAnimationRepeat(p1: Animator) {}
        })
        mAnimator!!.start()
        Tool.visibleViews(ANIMATION_DURATION, mLayoutView)
    }

    override fun onClick(view: View) {
        onMotionFolderClose()
        if (mTitleText.isFocused) {
            Display.Hide.keyboard(context, mItem as View)
        }
    }

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
    override fun onTextChanged(query: CharSequence, i: Int, i1: Int, i2: Int) {
        mItem!!.label = query.toString()
        Setup.itemManager().update(mItem!!)
        mItem!!.invalidate()
    }

    override fun afterTextChanged(editable: Editable) {}

    companion object {
        private const val AMOUNT_APPS_HORIZONTAL = 4
        private const val ANIMATION_DURATION: Long = 200
    }

}