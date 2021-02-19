package com.pukkol.launcher.viewutil

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import com.pukkol.launcher.R

class MotionRoundedBitmapLayout : DarkenRoundedBackgroundLayout {
    var mShaderPaint: Paint? = null
    var mAlphaPaint: Paint? = null
    private var backBitmap: Bitmap? = null
    private var backCanvas: Canvas? = null
    private val drawRect = Rect()
    private val source_bitmap: Bitmap? = null
    private val rect_view_in_bitmap = Rect()
    private val rect_parent_in_bitmap = Rect()
    private val parentHeight = 0f
    private val parentWidth = 0f
    private var mShouldBlurBackground = false
    private var alpha_blur = 1f
    private val rect_view_in_parent = Rect()

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
        mShaderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mAlphaPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        if (attrs != null) {
            val t = context.obtainStyledAttributes(attrs, R.styleable.MotionRoundedBitmapLayout)
            mShouldBlurBackground = t.getBoolean(R.styleable.MotionRoundedBitmapLayout_blur, true)
            t.recycle()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        backBitmap = null
        initBitmap()
    }

    private fun initBitmap() {
        if (backBitmap == null) {
            backBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            backCanvas = Canvas(backBitmap!!)
            drawRect.left = paddingLeft
            drawRect.top = paddingTop
            drawRect.right = drawRect.left + width - paddingLeft - paddingRight
            drawRect.bottom = drawRect.top + height - paddingTop - paddingBottom
        }
    }

    //
    //    @Override
    //    public void onWallpaperChanged(Bitmap original, Bitmap blur) {
    //        int[] s;
    //        s = Tool.getScreenSize(getContext());
    //        parentHeight = s[1];
    //        parentWidth = s[0];
    //        source_bitmap = blur;
    //        //  source_bitmap = BitmapEditor.getResizedBitmap(original,(int)parentWidth,(int)parentHeight);
    //        if (mShaderPaint == null) init(null);
    //        mShaderPaint.setShader(new BitmapShader(source_bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
    //
    //
    //        float ratio_parent = parentWidth / (parentHeight + 0.0f);
    //        float ratio_source = source_bitmap.getWidth() / (source_bitmap.getHeight() + 0.0f);
    //
    //        if (ratio_parent > ratio_source) {
    //            // crop height of source
    //            rect_parent_in_bitmap.Width = source_bitmap.getWidth();
    //            rect_parent_in_bitmap.Height = (int) (rect_parent_in_bitmap.Width * parentHeight / parentWidth);
    //
    //            rect_parent_in_bitmap.Left = 0;
    //            rect_parent_in_bitmap.Top = source_bitmap.getHeight() / 2 - rect_parent_in_bitmap.Height / 2;
    //        } else {
    //            // crop width of source
    //            // mean that
    //            rect_parent_in_bitmap.Height = source_bitmap.getHeight();
    //            rect_parent_in_bitmap.Width = (int) (rect_parent_in_bitmap.Height * parentWidth / parentHeight);
    //
    //            rect_parent_in_bitmap.Top = 0;
    //            rect_parent_in_bitmap.Left = source_bitmap.getWidth() / 2 - rect_parent_in_bitmap.Width / 2;
    //        }
    //        invalidate();
    //    }
    fun setBlurred(b: Boolean) {
        mShouldBlurBackground = b
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        initBitmap()
        if (source_bitmap != null && mShouldBlurBackground) drawMask(canvas)
        super.onDraw(canvas)
    }

    fun setAlphaBlurPaint(value: Float, shouldInvalidate: Boolean) {
        alpha_blur = value
        if (shouldInvalidate) invalidate()
    }

    private fun drawMask(canvas: Canvas) {
        val p = IntArray(2)
        getLocationOnScreen(p)
        val left = p[0] + paddingLeft
        val top = p[1] + paddingLeft
        val right = left + width - paddingLeft - paddingRight
        val bottom = top + height - paddingTop - paddingBottom
        rect_view_in_parent[left, top, right] = bottom
        rect_view_in_bitmap.left = (rect_parent_in_bitmap.left + rect_view_in_parent.left / (parentWidth + 0.0f) * rect_parent_in_bitmap.width()).toInt()
        rect_view_in_bitmap.right = rect_view_in_bitmap.left + (rect_view_in_parent.width() * rect_parent_in_bitmap.width() / parentWidth).toInt()
        rect_view_in_bitmap.top = (rect_parent_in_bitmap.top + rect_view_in_parent.top / (parentHeight + 0.0f) * rect_parent_in_bitmap.height()).toInt()
        rect_view_in_bitmap.bottom = rect_view_in_bitmap.top + (rect_view_in_parent.height() * rect_parent_in_bitmap.height() / (parentHeight + 0.0f)).toInt()
        if (!true) {
            mAlphaPaint!!.alpha = (255.0f * alpha_blur).toInt()
            canvas.drawBitmap(source_bitmap!!, rect_view_in_bitmap, drawRect, mAlphaPaint)
        } else {
            mShaderPaint!!.alpha = (255.0f * alpha_blur).toInt()
            canvas.save()
            //  canvas.scale(1,1);
            canvas.translate(-rect_view_in_parent.left.toFloat(), -rect_view_in_parent.top.toFloat())
            val path = BitmapEditor.RoundedRect(
                    rect_view_in_parent.left.toFloat(),
                    rect_view_in_parent.top.toFloat(), (
                    rect_view_in_parent.left + rect_view_in_parent.width()).toFloat(), (
                    rect_view_in_parent.top + rect_view_in_parent.height()).toFloat(),
                    maxRx * eachDP * number,
                    maxRy * eachDP * number,
                    round_type == ROUND_TYPE.ROUND_TOP
            )
            //  canvas.drawRect(getRectGraphic(drawRect), mSolidPaint);
            canvas.drawPath(path!!, mShaderPaint!!)
            canvas.restore()
        }
    }

    companion object {
        private const val TAG = "MotionRound"
    }
}