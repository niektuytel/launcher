package com.pukkol.launcher.ui.intro

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.pukkol.launcher.R
import com.pukkol.launcher.Setup
import com.pukkol.launcher.ui.intro.SlidePermissions.OnExtraListener
import com.pukkol.launcher.util.Display
import io.github.dreierf.materialintroscreen.MaterialIntroActivity

class IntroActivity : MaterialIntroActivity(), OnExtraListener {
    companion object {
        private const val UPDATE_NOTIFICATION = "update-notifications"
        private const val UPDATE_NOTIFICATION_COMMAND = "command"
        private const val UPDATE_NOTIFICATION_UPDATE = "update"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableLastSlideAlphaExitTransition(false)

        val controlSlide = SlideControl()
        addSlide(controlSlide)
        if (controlSlide.isRemotely) {
            // remotely controll is in development
            Log.d("TAG", "onCreate: IntroActivity on Remote is to connect someOnes device with this device")
        }
        addSlide(SlideAgreement())
        addSlide(SlidePermissions(this))
        addSlide(SlideEnd())
    }

    override fun notificationPermission() {
        if (notificationAccess) return

        // Request the permission
        val builder = MaterialDialog.Builder(this)
                .title(R.string.intro_customize_notification_title)
                .content(R.string.intro_customize_notification_summary)
                .negativeText(android.R.string.cancel)
                .positiveText(R.string.enable)

        builder.onPositive { _: MaterialDialog?, _: DialogAction? ->
                    Display.toast(this, getString(R.string.toast_notification_permission_required))
                    startActivity(
                            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    )
                }
                .show()
    }

    override val notificationAccess: Boolean
        get() {
            if (!Setup.deviceSettings().badgeNotification) return false
            val appList = NotificationManagerCompat.getEnabledListenerPackages(this)
            for (app in appList) {
                if (app == packageName) {
                    // Already allowed, so request a full update when returning to the home screen from another app.
                    val i = Intent(UPDATE_NOTIFICATION)
                    i.setPackage(packageName)
                    i.putExtra(UPDATE_NOTIFICATION_COMMAND, UPDATE_NOTIFICATION_UPDATE)
                    sendBroadcast(i)
                    return true
                }
            }
            return true
        }
}