package com.pukkol.launcher.ui.itemview

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import com.pukkol.launcher.ui.home.desktop.DesktopFragment

class WidgetHost(desktopFragment: DesktopFragment, hostId: Int) : AppWidgetHost(desktopFragment.requireContext(), hostId) {
    override fun onCreateView(context: Context, appWidgetId: Int, appWidget: AppWidgetProviderInfo): AppWidgetHostView {
        return WidgetHostView(context)
    }
}