package com.pukkol.launcher.ui.home.desktop.edit.wallpaper

import android.content.Context
import android.graphics.*
import android.util.Size

class WallpaperOptionContainer(private val mContext: Context, // todo: get chosen location and cut out the wallpaper
                               var bitmap: Bitmap?, var labelSize: Size) {
//    public set(bitmap)
//    {
//        field = bitmap
//    }
//




    var label: Bitmap?
    var padding = Rect(0, 0, 0, 0)
    var isCurrentWallpaper = false
        private set

    override fun toString(): String {
        return "WallpaperOptionContainer{" +
                "mContext=" + mContext +
                ", mBitmap=" + bitmap +
                ", mIcon=" + label +
                ", mSize=" + labelSize +
                ", mPadding=" + padding +
                ", mIsOccupied=" + isCurrentWallpaper +
                '}'
    }

    fun isCurrentWallpaper(currentWallpaper: Boolean) {
        isCurrentWallpaper = currentWallpaper
    }

    fun getLabel(wallpaper: Bitmap?): Bitmap? {
        if (wallpaper == null) return null
        val originalWidth = wallpaper.width
        val originalHeight = wallpaper.height
        var newWidth: Int
        var newHeight: Int
        var ratio: Double

        // get ratio
        run {
            if (originalHeight > originalWidth) {
                ratio = labelSize.width.toDouble() / originalWidth.toDouble()
                newWidth = labelSize.width
                newHeight = Math.ceil(originalHeight.toDouble() * ratio).toInt()
            } else {
                ratio = labelSize.height.toDouble() / originalHeight.toDouble()
                newHeight = labelSize.height
                newWidth = Math.ceil(originalWidth.toDouble() * ratio).toInt()
            }
        }
        newWidth = Math.min(wallpaper.width, newWidth)
        newHeight = Math.min(wallpaper.height, newHeight)

        // resize image
        var icon = Bitmap.createScaledBitmap(wallpaper, newWidth, newHeight, true)

        // cut image to rectangle format
        val pointX = if (newWidth >= newHeight) (newWidth - newHeight) / 2 else 0
        val pointY = if (newHeight >= newWidth) (newHeight - newWidth) / 2 else 0
        newWidth = Math.min(newHeight, newWidth)
        newHeight = Math.min(newHeight, newWidth)
        icon = Bitmap.createBitmap(icon!!, pointX, pointY, newWidth, newHeight, null, true)
        return icon
    }

    init {
        label = getLabel(bitmap)
    }
}