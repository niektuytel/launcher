package com.pukkol.launcher.ui.intro

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionRequestErrorListener
import com.karumi.dexter.listener.single.CompositePermissionListener
import com.karumi.dexter.listener.single.PermissionListener
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener
import com.pukkol.launcher.R
import kotlinx.android.synthetic.main.view_permission_item.view.*

class PermissionAdapter(private var mPermissions: List<Permission>) : RecyclerView.Adapter<PermissionAdapter.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.view_permission_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val permission = mPermissions[position]
        viewHolder.createHolder(permission)
    }

    override fun getItemCount(): Int {
        return mPermissions.size
    }

    fun updatePermissions(permissions: List<Permission>) {
        mPermissions = permissions
        notifyDataSetChanged()
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private val statusView: ImageView = itemView.image_premission_status
        private val titleView: TextView = itemView.text_permission_title
        private val layout: ConstraintLayout = itemView.layout_permission_item

        private lateinit var mPermission: Permission
        private lateinit var mPermissionListener: PermissionListener
        private lateinit var mPermissionErrorListener: PermissionRequestErrorListener

        fun createHolder(permission: Permission) {
            mPermission = permission
            mPermissionErrorListener = PermissionListenerError()
            mPermissionListener = CompositePermissionListener(
                    PermissionListenerAsync(this),
                    SnackbarOnDeniedPermissionListener.Builder
                            .with(itemView, R.string.intro_customize_permissions_missing_permission)
                            .withOpenSettingsButton(R.string.intro_customize_permissions_settings)
                            .build()
            )

            layout.setOnClickListener(this)
            showPermission(permission)
        }


        private fun showPermission(permission: Permission) {
            titleView.text = permission.permissionName
            when {
                permission.isGranted == null -> {
                    statusView.setColorFilter(Color.BLACK)
                }
                permission.isGranted!! -> {
                    statusView.setColorFilter(Color.GREEN)
                }
                else -> {
                    statusView.setColorFilter(Color.RED)
                }
            }
        }

        fun showPermissionGranted() {
            statusView.setColorFilter(Color.GREEN)
            mPermission.isGranted = true
        }

        fun showPermissionDenied() {
            statusView.setColorFilter(Color.RED)
            mPermission.isGranted = false
        }

        override fun onClick(v: View?) {
            Thread {
                Dexter.withContext(itemView.context)
                        .withPermission(mPermission.permissionManifest)
                        .withListener(mPermissionListener)
                        .withErrorListener(mPermissionErrorListener)
                        .onSameThread()
                        .check()
            }.start()
        }

    }
}