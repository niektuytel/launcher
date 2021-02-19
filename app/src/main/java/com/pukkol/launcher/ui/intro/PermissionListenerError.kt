package com.pukkol.launcher.ui.intro

import android.util.Log
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequestErrorListener

/**
 * Sample listener that shows how to handle permission request callbacks on a background thread
 */
class PermissionListenerError : PermissionRequestErrorListener {
    override fun onError(error: DexterError) {
        Log.e(PermissionListenerError::class.java.simpleName, "There was an error: $error")
    }
}