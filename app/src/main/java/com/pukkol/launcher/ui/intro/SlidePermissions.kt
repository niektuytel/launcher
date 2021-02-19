package com.pukkol.launcher.ui.intro

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.multi.SnackbarOnAnyDeniedMultiplePermissionsListener
import com.pukkol.launcher.R
import io.github.dreierf.materialintroscreen.SlideFragment
import kotlinx.android.synthetic.main.fragment_custom_permissions.view.*
import kotlin.collections.ArrayList

class SlidePermissions(private val mListener: OnExtraListener) : SlideFragment(), View.OnClickListener {
    private lateinit var cardView: CardView
    private lateinit var recyclerView: RecyclerView
    private lateinit var allPermissionsListener: MultiplePermissionsListener
    private lateinit var permissionListenerError: PermissionListenerError

    private var permissions = ArrayList<Permission>()
    private var adapter: PermissionAdapter? = null
    private var firstcall = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_custom_permissions, container, false)

        recyclerView = view.list_permissions!!
        recyclerView.overScrollMode = View.OVER_SCROLL_NEVER
        recyclerView.layoutManager = LinearLayoutManager(context)
        setPermissions()

        permissionListenerError = PermissionListenerError()
        allPermissionsListener = CompositeMultiplePermissionsListener(
                PermissionsListener(this),
                SnackbarOnAnyDeniedMultiplePermissionsListener.Builder.with(view, R.string.intro_customize_permissions_missing_permissions)
                        .withOpenSettingsButton(R.string.intro_customize_permissions_settings)
                        .build()
        )

        cardView = view.button_grant_all_permissions!!
        cardView.setOnClickListener(this)

        return view
    }

    override fun backgroundColor(): Int {
        return R.color.slide_custom_permissions_background
    }

    override fun buttonsColor(): Int {
        return R.color.slide_custom_permissions_buttons
    }

    override fun canMoveFurther(): Boolean {
        var isSucceeded = true
        for (permission in permissions) {
            if (permission.isGranted == null || !permission.isGranted!!) {
                isSucceeded = false
                break
            }
        }

        // extra
        if (firstcall) {
            mListener.notificationPermission()
            firstcall = false
        } else {
            mListener.notificationAccess
        }
        return isSucceeded
    }

    override fun cantMoveFurtherErrorMessage(): String {
        return getString(R.string.intro_customize_permissions_missing_permissions)
    }

    override fun onClick(v: View?) {
        val permissions: MutableList<String> = ArrayList()
        for (permission in this.permissions) {
            permissions.add(permission.permissionManifest)
        }

        Dexter.withContext(this.context)
                .withPermissions(permissions)
                .withListener(allPermissionsListener)
                .withErrorListener(permissionListenerError)
                .check()
    }

    fun showPermissionGranted(manifestName: String) {
        for (permission in permissions) {
            if (permission.permissionManifest == manifestName) {
                permission.isGranted = true
                break
            }
        }
        adapter!!.updatePermissions(permissions)
    }

    fun showPermissionDenied(manifestName: String) {
        for (permission in permissions) {
            if (permission.permissionManifest == manifestName) {
                permission.isGranted = false
                break
            }
        }
        adapter!!.updatePermissions(permissions)
    }

    private fun setPermissions() {
        val permission1 = Permission("camera", Manifest.permission.CAMERA)
        val permission2 = Permission("sim card", Manifest.permission.READ_CONTACTS)
        val permission3 = Permission("read storage", Manifest.permission.READ_EXTERNAL_STORAGE)
        val permission4 = Permission("write storage", Manifest.permission.WRITE_EXTERNAL_STORAGE)

        permissions.add(permission1)
        permissions.add(permission2)
        permissions.add(permission3)
        permissions.add(permission4)
        adapter = PermissionAdapter(permissions)
        recyclerView.adapter = adapter
    }

    interface OnExtraListener {
        fun notificationPermission()
        val notificationAccess: Boolean
    }
}