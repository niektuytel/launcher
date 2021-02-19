package com.pukkol.launcher.ui.home.desktop

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.pukkol.launcher.Setup
import com.pukkol.launcher.util.Definitions.WallpaperScroll
import me.everything.android.ui.overscroll.HorizontalOverScrollBounceEffectDecorator
import me.everything.android.ui.overscroll.IOverScrollDecor
import me.everything.android.ui.overscroll.IOverScrollUpdateListener
import me.everything.android.ui.overscroll.adapters.ViewPagerOverScrollDecorAdapter
import kotlin.collections.ArrayList

/**
 * # variables
 * [pages] ArrayList<CellContainer#ViewGroup> contains all pages data
 * [currentPage] current page that user displayed
 * [currentPageIndex] current page index
 *
 * # functions
 * [onRemoveEmptyPages] remove all empty pages, that do not contain any item init
 * [handleToPreviousPage] handles animation from current page to given previous page index
 * [handleToNextPage] handles animation from current page to given next page index
**/
@SuppressLint("ViewConstructor")
class PagerView(
        context: Context?,
        attr: AttributeSet?,
        private var params: ViewGroup.LayoutParams,
        private var desktopFragment: DesktopFragment,
        private var desktopLayout: DesktopLayout,
        private var pageIndicator: PageIndicatorView
) : ViewPager(context!!, attr), IOverScrollUpdateListener {
    val dragFields = PagerHelper(this)
    private var addLeftPage = false
    private var addRightPage = false

    var pages = ArrayList<CellContainer>()
        get() {
            if(field.isEmpty()) {
                field = Setup.itemManager().getDesktopItems(desktopFragment, desktopLayout, this.layoutParams)
            }

            return field
        }

    val currentPage: CellContainer?
        get() {
            return if (pages.size > 0) {
                pages[currentPageIndex]
            } else {
                null
            }
        }

    var currentPageIndex = -1
        get() {
            if(field == -1) {
                this.currentItem = Setup.deviceSettings().desktop().currentPage
            }

            field = this.currentItem
            return this.currentItem
        }
        set(value) {
            field = value
            this.currentItem = value
        }

    init {
        this.also {
            it.layoutParams = params
            it.overScrollMode = OVER_SCROLL_NEVER

            pageIndicator.setViewPager(it)
            adapter = PageAdapter(it)

            // over scroll effect
            HorizontalOverScrollBounceEffectDecorator(
                    ViewPagerOverScrollDecorAdapter(it)
            ).setOverScrollUpdateListener(it)
        }
    }

    fun onRemoveEmptyPages() {
        if (pages.size == 1) return
        val pagesLength = pages.size - 1 // size -> array

        for (i in pagesLength downTo 0) {
            if (pages[i].isEmpty) {
                // replace current page index
                currentPageIndex -= if (i < currentPageIndex) 1 else 0
                pages.removeAt(i)
                adapter!!.notifyDataSetChanged()
            }
        }

        addLeftPage = false
        addRightPage = false
    }

    fun handleToPreviousPage() {
        onPageSwitch(currentPageIndex - 1)
        postDelayed(Thread.currentThread(), 1000)
    }

    fun handleToNextPage() {
        onPageSwitch(currentPageIndex + 1)
        postDelayed(Thread.currentThread(), 1000)
    }

    override fun onOverScrollUpdate(decor: IOverScrollDecor, state: Int, offset: Float) {
        pageIndicator.onShowIndicator(offset, offset)
    }

    override fun onPageScrolled(position: Int, offset: Float, offsetPixels: Int) {
        val scroll = Setup.deviceSettings().desktop().wallpaperScroll
        var xOffset = (position + offset) / (pages.size - 1)
        if (scroll == WallpaperScroll.INVERSE) {
            xOffset = 1f - xOffset
        } else if (scroll == WallpaperScroll.OFF) {
            xOffset = 0.5f
        }

        pageIndicator.onShowIndicator(offsetPixels.toFloat(), 0.0f)
        dragFields.setVisibleState(pages, currentPageIndex)

        val wallpaperManager = WallpaperManager.getInstance(context)
        wallpaperManager.setWallpaperOffsets(windowToken, xOffset, 0.0f)

        super.onPageScrolled(position, offset, offsetPixels)
    }

    private fun onPageSwitch(futurePage: Int) {
        val prevPage = currentPageIndex
        var newPage = futurePage
        val lastPage = pages.size - 1 // size -> length
        dragFields.setVisibleState(pages, prevPage)
        if (newPage > lastPage) {
            addRightPage = if (addRightPage) return else true
            pages.add(desktopLayout.onCreatePage(params))
            adapter!!.notifyDataSetChanged()
        } else if (newPage < 0) {
            addLeftPage = if (addLeftPage) return else true
            pages.add(0, desktopLayout.onCreatePage(params))
            adapter!!.notifyDataSetChanged()
            newPage = 0
        }

        currentPageIndex = newPage
        this.invalidate()
        pageIndicator.invalidate()
    }

    class PageAdapter(private var pagerView: PagerView) : PagerAdapter() {
        init {
            pagerView.pages.clear()
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val layout = pagerView.pages[position]
            container.addView(layout)
            return layout
        }

        override fun getCount(): Int {
            return pagerView.pages.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj
        }
    }
}