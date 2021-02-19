package com.pukkol.launcher.interfaces

import android.graphics.Bitmap

interface IWallpaperListener {
    fun onWallpaperPick()
    fun onWallpaperChanged(bitmap: Bitmap, onHomeScreen: Boolean, onLockScreen: Boolean)
}