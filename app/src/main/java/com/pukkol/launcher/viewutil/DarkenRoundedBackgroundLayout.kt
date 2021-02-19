package com.pukkol.launcher.viewutil

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import com.pukkol.launcher.R
import com.pukkol.launcher.util.Display

@Deprecated(" ")
open class DarkenRoundedBackgroundLayout : ConstraintLayout {
    protected val maxRy = 14f
    protected val maxRx = 14f
    protected var mSolidPaint: Paint? = null
    protected var eachDP = 0f
    protected var round_type = ROUND_TYPE.ROUND_ALL
    protected var layoutAlpha = 1f
    protected var number = 0f
    var darken = 0f
        private set
    private var drawDarkenPaint: Paint? = null
    private var color_background = 0

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
        val t = context.obtainStyledAttributes(attrs, R.styleable.DarkenRoundedBackgroundLayout)

//        ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
//        layoutParams.height = Tool.getDisplaySize(HomeActivity.Companion.getLauncher()).getHeight();
        // this.requestLayout();
        color_background = t.getColor(R.styleable.DarkenRoundedBackgroundLayout_round_back_color, Color.WHITE)
        round_type = if (t.getInt(R.styleable.DarkenRoundedBackgroundLayout_round_type, 1) == 1) ROUND_TYPE.ROUND_ALL else ROUND_TYPE.ROUND_TOP
        number = t.getFloat(R.styleable.DarkenRoundedBackgroundLayout_round_number, 0f)
        layoutAlpha = t.getFloat(R.styleable.DarkenRoundedBackgroundLayout_background_alpha, 1f)
        drawDarkenPaint = Paint()
        drawDarkenPaint!!.style = Paint.Style.FILL
        drawDarkenPaint!!.isAntiAlias = true
        mSolidPaint = Paint()
        mSolidPaint!!.style = Paint.Style.FILL
        mSolidPaint!!.isAntiAlias = true
        t.recycle()
    }

    fun getEditTextChild(resourceID: Int): EditText {
        return findViewById(resourceID)
    }

    fun setDarken(darken: Float, shouldDraw: Boolean) {
        if (darken >= 0 && darken <= 1) {
            this.darken = darken
            if (shouldDraw) invalidate()
        }
    }

    fun setRoundType(type: ROUND_TYPE, shouldInvalidate: Boolean) {
        round_type = type
        if (shouldInvalidate) invalidate()
    }

    fun setBackGroundColor(color: Int) {
        color_background = color
    }

    fun setAlphaBackground(value: Float) {
        layoutAlpha = value
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //   canvas.drawColor(Color.WHITE);
        //   if(backColor1!=0) canvas.drawColor(backColor1);
        //   if(backColor2!=0) canvas.drawColor(backColor2);
        mSolidPaint!!.color = color_background
        mSolidPaint!!.alpha = (layoutAlpha * 255f).toInt()
        drawContent(canvas, mSolidPaint)
    }

    fun setRoundNumber(number: Float, shouldDraw: Boolean) {
        if (this.number != number) {
            this.number = number
        }
        if (shouldDraw) invalidate()
    }

    private fun drawContent(canvas: Canvas, paint: Paint?) {
        if (eachDP == 0f) eachDP = Display.dp2px(1f).toFloat()
        val width = width
        val height = height
        canvas.drawPath(
                BitmapEditor.RoundedRect(0f, 0f,
                        width.toFloat(), height.toFloat(),
                        maxRx * eachDP * number,
                        maxRy * eachDP * number,
                        round_type == ROUND_TYPE.ROUND_TOP
                ),
                paint!!
        )
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        // int color4White = (int)( darken * 255.0f);
        //  if(color4White>255) color4White = 255; else if(color4White<0) color4White =0;
        var color4Black = (255.0f * darken).toInt()
        if (color4Black > 255) {
            color4Black = 255
        }
        drawDarkenPaint!!.color = color4Black shl 24
        drawContent(canvas, drawDarkenPaint)
    }

    enum class ROUND_TYPE {
        ROUND_ALL, ROUND_TOP
    }
}