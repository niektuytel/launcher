package com.pukkol.launcher.services

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class DeviceAdminReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "DeviceAdmin received")
    }

    companion object {
        private val TAG = DeviceAdminReceiver::class.java.simpleName
    }
}