package com.pukkol.app_settings

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_settings_app.view.*

class AppAdapter(private val context : Context) : RecyclerView.Adapter<AppAdapter.ViewHolder>()
{
    private var mRemovedItems = ArrayList<App>()
    private val mItems = ArrayList<App>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view : View = LayoutInflater.from(context).inflate(R.layout.view_settings_app, parent, false)
        return ViewHolder(view, this)
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app : App = mItems[position]

        if(!mRemovedItems.contains(app)) {
            holder.onCreate(app, position)
        } else {
            Log.d("ItemAppAdapter", "onBindViewHolder: ${app.label} is removed")
        }
    }

    fun addItem(app : App) {
        mItems.add(app)
        this.notifyDataSetChanged()
    }

    fun removeItem(app : App, position : Int ) {
        mRemovedItems.add(app)
        mItems.remove(app)

        this.notifyItemRemoved(position)
        this.notifyItemRangeChanged(position, mItems.size)
    }

    fun getRemovedItems() : ArrayList<String> {
        val apps = ArrayList<String>()
        for (app in mRemovedItems) {
            apps.add(app.packageName)
        }

        return apps
    }

    class ViewHolder(view: View, adapter : AppAdapter) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private val sTag = javaClass.name

        private var mApp : App? = null
        private var mPosition : Int = 0
        private var mAdapter : AppAdapter = adapter
        private var mLabel : TextView = view.text_app_label
        private var mIcon : ImageView = view.image_app_icon
        private var mRemoveApp : ImageButton = view.button_app_remove

        init {
            mRemoveApp.setOnClickListener(this)
        }

        fun onCreate(app : App, position : Int) {
            mApp = app
            mPosition = position
            mLabel.text = app.label
            mIcon.setImageBitmap(app.icon)
        }

        override fun onClick(view: View?) {
            when (view!!.id) {
                mRemoveApp.id -> {
                    onConfirmDialog()
                }
                else -> {
                    Log.e(sTag, "onClick: ${view.id} is not founded")
                }
            }
        }

        private fun onConfirmDialog() {
            val builder = AlertDialog.Builder(mAdapter.context, R.style.AlertDialogTheme)
            builder.setMessage(R.string.confirm_remove_app_text)
                    .setCancelable(false)
                    .setPositiveButton(R.string.confirm_remove_app_yes) {
                        dialog, _ ->
                            mApp?.let { mAdapter.removeItem(it, mPosition) }
                            dialog.dismiss()// Remove App
                    }
                    .setNegativeButton(R.string.confirm_remove_app_no) {
                        dialog, _ ->
                            dialog.dismiss()// Cancel
                    }

            builder.create().show()
        }
    }
}