package com.pukkol.launcher.ui.intro

import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

/**
 * Sample listener that shows how to handle permission request callbacks on a background thread
 */
open class PermissionListener(private val mHolder: PermissionAdapter.ViewHolder) : PermissionListener {
    override fun onPermissionGranted(response: PermissionGrantedResponse) {
        mHolder.showPermissionGranted()
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse) {
        mHolder.showPermissionDenied()
    }

    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {}
}