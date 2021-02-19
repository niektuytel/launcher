package com.pukkol.launcher.viewutil

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.pukkol.launcher.R
import com.pukkol.launcher.util.Display

open class MotionRadiusLayout : ConstraintLayout {
    private var mBorderPaint = Paint()
    private var mRadiusType: RadiusType? = null
    private var mRadiusValue = 0.0
    private var mColor = 0

    enum class RadiusType {
        RADIUS_TOP, RADIUS_ALL, RADIUS_BOTTOM
    }

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    fun init(attrs: AttributeSet?) {
        // get attribute sets
        val t = context.obtainStyledAttributes(attrs, R.styleable.MotionRadiusLayout)
        setRadiusType(t.getInt(R.styleable.MotionRadiusLayout_radius_type, RadiusType.RADIUS_ALL.ordinal))
        mRadiusValue = t.getFloat(R.styleable.MotionRadiusLayout_radius_value, 0.1f).toDouble()
        mColor = t.getColor(R.styleable.MotionRadiusLayout_background_color, Color.BLACK)
        t.recycle()
        mBorderPaint = Paint()
        mBorderPaint.style = Paint.Style.FILL
        mBorderPaint.isAntiAlias = true
    }

    var radius: Double
        get() = mRadiusValue
        set(radius) {
            mRadiusValue = radius
            this.invalidate()
        }

    fun getBackgroundColor(): Int {
        return mColor
    }

    override fun setBackgroundColor(color: Int) {
        mColor = color
        this.invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // border color
        if (mBorderPaint.color != mColor) {
            mBorderPaint.color = mColor
        }
        val dp = Display.dp2px(1f).toDouble()
        val maxRadius = Math.min(width, height) * dp / 2
        val radius = (maxRadius * mRadiusValue).toFloat()
        canvas.drawPath(
                BitmapEditor.RoundedRect(0f, 0f, this.width.toFloat(), this.height.toFloat(), radius, radius,
                        mRadiusType == RadiusType.RADIUS_TOP
                ),
                mBorderPaint
        )
    }

    private fun setRadiusType(index: Int) {
        if (index == RadiusType.RADIUS_TOP.ordinal) {
            mRadiusType = RadiusType.RADIUS_TOP
        } else if (index == RadiusType.RADIUS_ALL.ordinal) {
            mRadiusType = RadiusType.RADIUS_ALL
        } else if (index == RadiusType.RADIUS_BOTTOM.ordinal) {
            mRadiusType = RadiusType.RADIUS_BOTTOM
        }
    }
}