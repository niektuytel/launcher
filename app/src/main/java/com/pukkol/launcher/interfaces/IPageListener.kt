package com.pukkol.launcher.interfaces

interface IPageListener {
    // void onResponseWidget(int requestCode, Intent data);
    // void onDeleteWidget(int widgetId);
    fun onPickWidget()
    fun onLaunchWallpaper()
    fun onLaunchSettings()
    fun showDesktop()
}