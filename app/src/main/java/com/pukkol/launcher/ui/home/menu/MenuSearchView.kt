package com.pukkol.launcher.ui.home.menu

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.EditText
import com.pukkol.launcher.R
import com.pukkol.launcher.util.Display
import com.pukkol.launcher.util.Tool
import com.pukkol.launcher.viewutil.MotionRadiusLayout

/**
 * @author okido
 * @since 1/22/2021
 */
class MenuSearchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : MotionRadiusLayout(context, attrs, defStyleAttr), TextWatcher {
    private var mBackgroundColor = 0
    private var mSearchInput: EditText? = null
    private var mCallback: OnSearchCallback? = null
    private val mStatusBarHeight: Int
    private val mRadiusInteractFrom: Int
    private val mMaxRadius: Double
    var isSearching = false
        private set

    fun init(searchInput: EditText?) {
        mSearchInput = searchInput
        mSearchInput!!.addTextChangedListener(this)
    }

    fun initView() {
        isSearching = false
        if (mSearchInput!!.text.toString() != "") {
            mSearchInput!!.setText("")
            mSearchInput!!.clearFocus()
        }
    }

    fun setOnLayoutListener(pageView: MenuPageView?) {
        mCallback = pageView
        isSearching = false
    }

    fun updateContentColor() {
        val average = Display.AverageColor.searchBox
        val newColor = Tool.getIntOppositeColor(average)

        mBackgroundColor = newColor
        mSearchInput!!.setTextColor(newColor)
        mSearchInput!!.setHintTextColor(newColor)
    }

    fun setRadius(positionY: Float) {
        if (positionY > mRadiusInteractFrom) return
        val percentage = Math.max(positionY / mRadiusInteractFrom, 0f)
        radius = Math.min(mMaxRadius, percentage.toDouble())
        updatePadding(percentage)
    }

    private fun updatePadding(percentage: Float) {
        val paddingTop = mStatusBarHeight * Math.abs(percentage - 1.00f)
        val newTopPadding = (this.resources.getDimension(R.dimen.menu_searchBox_padding) + paddingTop).toInt()
        setPadding(
                this.paddingLeft,
                newTopPadding,
                this.paddingRight,
                this.paddingBottom
        )
    }

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
        mCallback!!.onQuerySearch(charSequence.toString())
        isSearching = true
    }

    override fun afterTextChanged(editable: Editable) {}
    fun isSearching(isSearching: Boolean) {
        this.isSearching = isSearching
    }

    interface OnSearchCallback {
        fun onQuerySearch(query: String?)
    }

    init {
        mStatusBarHeight = Display.STATUSBAR_HEIGHT
        val typedValue = TypedValue()
        resources.getValue(R.dimen.menu_searchBox_maxRadius, typedValue, true)
        mMaxRadius = typedValue.float.toDouble()
        val display = Display.DISPLAY_SIZE
        mRadiusInteractFrom = display!!.height / 4
    }
}