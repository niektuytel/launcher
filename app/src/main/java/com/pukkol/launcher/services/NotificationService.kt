package com.pukkol.launcher.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Message
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.pukkol.launcher.interfaces.INotificationListener
import java.util.*

/**
 * receive all notifications of device whats is getting inside
 * @Author Niek Tuytel (Okido)
 */
class NotificationService : NotificationListenerService() {
    private var mNotificationReceiver: NotificationListenerReceiver? = null
    private var mIsConnected = false
    private val mMonitorHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == EVENT_UPDATE_CURRENT_NOS) {
                updateCurrentNotifications()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (mNotificationReceiver == null) {
            mNotificationReceiver = NotificationListenerReceiver()
            val filter = IntentFilter(UPDATE_NOTIFICATIONS_ACTION)
            registerReceiver(mNotificationReceiver, filter)
        }
    }

    override fun onDestroy() {
        unregisterReceiver(mNotificationReceiver)
        mNotificationReceiver = null
    }

    override fun onListenerConnected() {
        Log.d(TAG, "Listener connected")
        mIsConnected = true
        mMonitorHandler.sendMessage(mMonitorHandler.obtainMessage(EVENT_UPDATE_CURRENT_NOS))
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Some apps do not track total notifications in their StatusBarNotification; given this
        // is an onNotificationPosted, ensure we have a minimum count to display the badge, otherwise
        // it will be removed which is counter-intuitive.
        var notificationCount = sbn.notification.number
        if (notificationCount == 0) {
            notificationCount = 1
        }
        processCallback(sbn.packageName, notificationCount)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        processCallback(sbn.packageName, 0)
    }

    private fun processCallback(packageName: String, index: Int) {
        Log.d(TAG, "processCallback($packageName) -> $index")
        val callbacks = mCurrentNotifications[packageName]
        if (callbacks != null) {
            for (callback in callbacks) {
                callback.setNotificationView(index)
            }
        }
    }

    private fun updateCurrentNotifications() {
        if (mIsConnected) {
            try {
                val activeNos = activeNotifications
                var packageName = ""
                var notificationCount = 0
                for (activeNo in activeNos) {
                    val pkg = activeNo.packageName
                    if (packageName != pkg) {
                        packageName = pkg
                        notificationCount = 0
                    }
                    val count = activeNo.notification.number
                    if (count == 0) {
                        notificationCount++
                    } else {
                        notificationCount = Math.max(notificationCount, count)
                    }
                    processCallback(packageName, notificationCount)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected exception when updating notifications: $e")
            }
        }
    }

    internal inner class NotificationListenerReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getStringExtra(UPDATE_NOTIFICATIONS_COMMAND) == UPDATE_NOTIFICATIONS_UPDATE) {
                updateCurrentNotifications()
            }
        }
    }

    companion object {
        const val UPDATE_NOTIFICATIONS_ACTION = "update-notifications"
        const val UPDATE_NOTIFICATIONS_COMMAND = "command"
        const val UPDATE_NOTIFICATIONS_UPDATE = "update"
        private val TAG = NotificationService::class.java.simpleName
        private const val EVENT_UPDATE_CURRENT_NOS = 0
        private val mCurrentNotifications = HashMap<String, ArrayList<INotificationListener>>()
        @JvmStatic
        fun setNotificationCallback(packageName: String, callback: INotificationListener) {
            var callbacks = mCurrentNotifications[packageName]
            if (callbacks != null) {
                callbacks.add(callback)
            } else {
                callbacks = ArrayList(1)
                callbacks.add(callback)
                mCurrentNotifications[packageName] = callbacks
            }
        }
    }
}