package com.pukkol.app_settings

import android.content.pm.LauncherActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

class App {
    var icon: Bitmap
    var label: String
    var packageName: String

    constructor(pm: PackageManager?, info: ResolveInfo) {
        icon = drawableToBitmap(info.loadIcon(pm))
        label = info.loadLabel(pm).toString()
        packageName = info.activityInfo.packageName
    }

    constructor(info: LauncherActivityInfo) {
        icon = drawableToBitmap(info.getIcon(0))
        label = info.label.toString()
        packageName = info.componentName.packageName
    }

    private fun drawableToBitmap(drawable: Drawable?): Bitmap {
        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }
        val bitmap: Bitmap = if (drawable?.intrinsicWidth!! <= 0 || drawable.intrinsicHeight <= 0) {
            // single color bitmap will be created
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        } else {
            Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }
}
