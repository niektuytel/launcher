package com.pukkol.launcher.viewutil

import android.content.Context
import android.util.AttributeSet
import android.util.Size
import androidx.constraintlayout.widget.ConstraintLayout
import com.pukkol.launcher.R
import com.pukkol.launcher.util.Display

open class ConstraintLayoutView : ConstraintLayout {
    private var mLayoutWeightWidth = -1
    private var mLayoutWeightHeight = -1
    private var mDisplaySize: Size? = null

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        mDisplaySize = Display.DISPLAY_SIZE // DisplayUtil.setDisplaySize(this.getContext());
        val t = context.obtainStyledAttributes(attrs, R.styleable.ConstraintLayoutView)
        mLayoutWeightWidth = t.getInt(R.styleable.ConstraintLayoutView_layout_weight_width, -1)
        mLayoutWeightHeight = t.getInt(R.styleable.ConstraintLayoutView_layout_weight_height, -1)
        t.recycle()
        updateLayoutSize()
    }

    fun setLayoutWeight(weight: Int) {
        mLayoutWeightHeight = weight
        mLayoutWeightWidth = mLayoutWeightHeight
        updateLayoutSize()
    }

    fun setLayoutWeight(weightWidth: Int, weightHeight: Int) {
        mLayoutWeightWidth = weightWidth
        mLayoutWeightHeight = weightHeight
        updateLayoutSize()
    }

    fun setLayoutWeightWidth(weightWidth: Int) {
        mLayoutWeightWidth = weightWidth
        updateLayoutSize()
    }

    fun setLayoutWeightHeight(weightHeight: Int) {
        mLayoutWeightHeight = weightHeight
        updateLayoutSize()
    }

    private fun updateLayoutSize() {
        val lp = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        )
        if (mLayoutWeightWidth != -1) {
            lp.width = mDisplaySize!!.width / mLayoutWeightWidth
        }
        if (mLayoutWeightHeight != -1) {
            lp.height = mDisplaySize!!.height / mLayoutWeightHeight
        }
        this.layoutParams = lp
    }
}