package com.pukkol.launcher.ui.itemview

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.pukkol.launcher.R
import com.pukkol.launcher.data.model.Item
import com.pukkol.launcher.util.Tool

class FolderDrawable(private val mContext: Context, item: Item) {
    private val mDrawable: Drawable
    private lateinit var mAppIcons: Array<Bitmap?>
    private var mFolderPath: Path? = null
    private var mPaintFolderBackground: Paint? = null
    private var mPaintAppIcon: Paint? = null
    private var mPaintFolderBorder: Paint? = null
    private var mFolderRadius = 0f
    private var mScaleFactor = 1f
    private var mFolderSize = 0
    private var mFolderCenter = 0
    private var mAppIconsCount = 0
    private var mAppPadding = 0
    private var mAppSize = 0
    private var mNeedAnimate = false
    private var mNeedAnimateScale = false
    private fun initFolder(icons: Array<Bitmap?>, iconsCount: Int) {
        val folderSize = mContext.resources.getDimensionPixelSize(R.dimen.app_icon_width)
        mAppIcons = icons
        mAppIconsCount = iconsCount
        mAppSize = Math.round(folderSize / 2f)
        mAppPadding = (folderSize / 25f).toInt()
        mFolderSize = folderSize
        mFolderCenter = folderSize / 2
        mFolderRadius = mAppSize / 2.00f / 2f
        mFolderPath = Tool.RoundedRect(folderSize.toFloat(), folderSize.toFloat(), mFolderRadius)
        mPaintFolderBackground = Paint()
        mPaintFolderBackground!!.color = Color.WHITE
        mPaintFolderBackground!!.alpha = 150
        mPaintFolderBackground!!.isAntiAlias = true
        mPaintFolderBorder = Paint()
        mPaintFolderBorder!!.color = Color.WHITE
        mPaintFolderBorder!!.style = Paint.Style.STROKE
        mPaintFolderBorder!!.strokeWidth = 1f
        mPaintFolderBorder!!.isAntiAlias = true
        mPaintAppIcon = Paint()
        mPaintAppIcon!!.isAntiAlias = true
        mPaintAppIcon!!.isFilterBitmap = true
    }

    fun popUp() {
        mNeedAnimate = true
        mNeedAnimateScale = true
        mDrawable.invalidateSelf()
    }

    fun popBack() {
        mNeedAnimate = false
        mNeedAnimateScale = false
        mDrawable.invalidateSelf()
    }

    fun onDrawFolder(canvas: Canvas) {
        canvas.save()
        mScaleFactor = if (mNeedAnimateScale) {
            Tool.clampFloat(mScaleFactor - 0.09f, 0.5f, 1f)
        } else {
            Tool.clampFloat(mScaleFactor + 0.09f, 0.5f, 1f)
        }
        canvas.scale(mScaleFactor, mScaleFactor, mFolderCenter.toFloat(), mFolderCenter.toFloat())

        // draw folder background
        canvas.drawPath(mFolderPath!!, mPaintFolderBackground!!)

        // draw folder icons
        drawFolderIcons(canvas)

        // draw folder line around
        canvas.drawPath(mFolderPath!!, mPaintFolderBorder!!)
        canvas.restore()
        if (mNeedAnimate) {
            mPaintAppIcon!!.alpha = Tool.clampInt(mPaintAppIcon!!.alpha - 25, 0, 255)
            mDrawable.invalidateSelf()
        } else if (mPaintAppIcon!!.alpha != 255) {
            mPaintAppIcon!!.alpha = Tool.clampInt(mPaintAppIcon!!.alpha + 25, 0, 255)
            mDrawable.invalidateSelf()
        }
    }

    private fun drawFolderIcons(canvas: Canvas) {
        // catch odd numbers
        val oddNum = mAppIconsCount % 2
        for (i in 0 until mAppIconsCount) {
            val icon = mAppIcons[i] ?: break

            // odd number
            var left = mAppPadding
            left += if (mAppIconsCount - oddNum == i) {
                mAppSize / 2
            } else {
                mAppSize * (i % 2)
            }

            // 2 apps need to center in icon
            var top = mAppPadding
            top += if (mAppIconsCount == 2) {
                mAppSize / 2
            } else {
                mAppSize * Math.floor(i / 2.00).toInt()
            }
            val right = left + (mAppSize - mAppPadding * 2)
            val bottom = top + (mAppSize - mAppPadding * 2)

            // icon in folder
            drawIcon(canvas, icon, left, top, right, bottom, mPaintAppIcon!!)
        }
    }

    private fun drawIcon(canvas: Canvas, bmp: Bitmap, left: Int, top: Int, right: Int, bottom: Int, paint: Paint) {
        val icon: Drawable = BitmapDrawable(mContext.resources, bmp)
        icon.setBounds(left, top, right, bottom)
        icon.isFilterBitmap = true
        icon.alpha = paint.alpha
        icon.draw(canvas)
    }

    val icon: Bitmap
        get() = Tool.drawableToBitmap(mDrawable)!!

    companion object {
        private const val MAX_FOLDER_ICONS = 4
    }

    init {
        mDrawable = object : Drawable() {
            override fun draw(canvas: Canvas) {
                if (item.folderApps != null) { // Folder
                    onDrawFolder(canvas)
                }
            }

            override fun setAlpha(i: Int) {}
            override fun setColorFilter(colorFilter: ColorFilter?) {}
            override fun getIntrinsicWidth(): Int {
                return mFolderSize
            }

            override fun getIntrinsicHeight(): Int {
                return mFolderSize
            }

            override fun getOpacity(): Int {
                return PixelFormat.TRANSPARENT
            }
        }
        if (item.folderApps != null) { // Folder
            val amount = Math.min(MAX_FOLDER_ICONS, item.folderApps!!.size)
            val icons = arrayOfNulls<Bitmap>(amount)
            for (i in 0 until amount) {
                icons[i] = null
            }
            initFolder(icons, amount)
            for (i in 0 until amount) {
                val app = item.folderApps!![i]
                mAppIcons[i] = app.icon
            }
        }
    }
}