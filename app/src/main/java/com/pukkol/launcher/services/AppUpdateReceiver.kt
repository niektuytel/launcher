package com.pukkol.launcher.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pukkol.launcher.Setup

class AppUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Setup.appManager().onAppUpdated(context, intent)
    }
}