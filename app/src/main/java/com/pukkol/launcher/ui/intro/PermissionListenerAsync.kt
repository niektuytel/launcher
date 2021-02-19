package com.pukkol.launcher.ui.intro

import android.os.Handler
import android.os.Looper
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.*
import com.pukkol.launcher.ui.intro.PermissionListenerAsync

/**
 * Sample listener that shows how to handle permission request callbacks on a background thread
 */
class PermissionListenerAsync(holder: PermissionAdapter.ViewHolder) : PermissionListener(holder) {
    private val handler = Handler(Looper.getMainLooper())
    override fun onPermissionGranted(response: PermissionGrantedResponse) {
        handler.post { super@PermissionListenerAsync.onPermissionGranted(response) }
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse) {
        handler.post { super@PermissionListenerAsync.onPermissionDenied(response) }
    }

    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
        handler.post {
            super@PermissionListenerAsync.onPermissionRationaleShouldBeShown(
                    permission, token)
        }
    }
}