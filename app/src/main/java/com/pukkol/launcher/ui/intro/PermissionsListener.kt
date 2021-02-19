package com.pukkol.launcher.ui.intro

import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class PermissionsListener(private val mPermissionSlidePermissions: SlidePermissions) : MultiplePermissionsListener {
    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
        for (response in report.grantedPermissionResponses) {
            mPermissionSlidePermissions.showPermissionGranted(response.permissionName)
        }
        for (response in report.deniedPermissionResponses) {
            mPermissionSlidePermissions.showPermissionDenied(response.permissionName)
        }
    }

    override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {}
}