package com.pukkol.launcher.util

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable

@Deprecated("not been used in the code, so it is deprecated")
class CircleDrawer(context: Context?, icon: Drawable, colorIcon: Int, colorBackground: Int, alphaBackground: Int) : Drawable() {
    private val mIconSize: Int
    private val mIconSizeReal: Int
    private val mIconPadding: Int
    private val mIconColor: Int
    private var mIcon: Bitmap?
    private var mIconToFade: Bitmap? = null
    private val mPaint: Paint
    private val mPaint2: Paint
    private val mScaleStep = 0.08f
    private var mCurrentScale = 1f
    private var mHidingOldIcon = false
    fun setIcon(icon: Drawable) {
        mIconToFade = mIcon
        mHidingOldIcon = true
        icon.setColorFilter(mIconColor, PorterDuff.Mode.SRC_ATOP)
        mIcon = Tool.drawableToBitmap(icon)
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        canvas.drawCircle(mIconSize.toFloat() / 2, mIconSize.toFloat() / 2, mIconSize.toFloat() / 2, mPaint)
        if (mIconToFade != null) {
            canvas.save()
            if (mHidingOldIcon) mCurrentScale -= mScaleStep else mCurrentScale += mScaleStep
            mCurrentScale = Tool.clampFloat(mCurrentScale, 0f, 1f)
            canvas.scale(mCurrentScale, mCurrentScale, mIconSize.toFloat() / 2, mIconSize.toFloat() / 2)
            canvas.drawBitmap(if (mHidingOldIcon) mIconToFade!! else mIcon!!, mIconSize.toFloat() / 2 - mIconSizeReal.toFloat() / 2, mIconSize.toFloat() / 2 - mIconSizeReal.toFloat() / 2, mPaint2)
            canvas.restore()
            if (mCurrentScale == 0f) {
                mHidingOldIcon = false
            }
            if (!mHidingOldIcon && mScaleStep == 1f) {
                mIconToFade = null
            }
            invalidateSelf()
        } else {
            canvas.drawBitmap(mIcon!!, mIconSize.toFloat() / 2 - mIconSizeReal.toFloat() / 2, mIconSize.toFloat() / 2 - mIconSizeReal.toFloat() / 2, mPaint2)
        }
    }

    override fun getIntrinsicWidth(): Int {
        return mIconSize
    }

    override fun getIntrinsicHeight(): Int {
        return mIconSize
    }

    override fun setAlpha(i: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }

    init {
        icon.setColorFilter(colorIcon, PorterDuff.Mode.SRC_ATOP)
        mIcon = Tool.drawableToBitmap(icon)
        mIconPadding = Display.dp2px(6f)
        mIconColor = colorIcon
        mIconSizeReal = icon.intrinsicHeight
        mIconSize = icon.intrinsicHeight + mIconPadding * 2
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.color = colorBackground
        mPaint.alpha = alphaBackground
        mPaint.style = Paint.Style.FILL
        mPaint2 = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint2.color = colorIcon
        mPaint2.isFilterBitmap = true
    }
}