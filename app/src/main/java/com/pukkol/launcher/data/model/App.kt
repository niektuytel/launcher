package com.pukkol.launcher.data.model

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.pm.LauncherActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.os.UserHandle
import com.pukkol.launcher.util.Tool

/**
 * App model represents the data for every App icon in the launcher
 * @Author Niek Tuytel (Okido)
 */
class App {
    var icon: Bitmap
    var label: String
    var packageName: String

    /**
     * Getters & Setters
     */
    var className: String

    //    public Limit getLimit() { return mLimit; }
    var userHandle: UserHandle? = null

    // private Limit mLimit;
    // intent for shortcuts and apps
    // private Intent mIntent;
    constructor(pm: PackageManager?, info: ResolveInfo) {
        icon = Tool.drawableToBitmap(info.loadIcon(pm))!!
        label = info.loadLabel(pm).toString()
        packageName = info.activityInfo.packageName
        className = info.activityInfo.name
    }

    @SuppressLint("NewApi")
    constructor(pm: PackageManager?, info: LauncherActivityInfo) {
        icon = Tool.drawableToBitmap(info.getIcon(0))!!
        label = info.label.toString()
        packageName = info.componentName.packageName
        className = info.name
    }

    override fun equals(`object`: Any?): Boolean {
        return if (`object` is App) {
            packageName == `object`.packageName
        } else {
            false
        }
    }

    val componentName: String
        get() = ComponentName(packageName, className).toString()

    fun setComponentName(componentName: ComponentName) {
        packageName = componentName.packageName
        className = componentName.className
    }

    override fun hashCode(): Int {
        var result = icon.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + className.hashCode()
        result = 31 * result + (userHandle?.hashCode() ?: 0)
        result = 31 * result + componentName.hashCode()
        return result
    }
    //
    //    public void setLimit(Limit limit) { mLimit = limit; }
}