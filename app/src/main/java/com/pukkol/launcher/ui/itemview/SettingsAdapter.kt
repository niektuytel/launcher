package com.pukkol.launcher.ui.itemview

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pukkol.launcher.R
import com.pukkol.launcher.data.model.App

class SettingsAdapter(private val mLocalDataSet: List<App>?, private val mCallback: OnAdapterListener) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.view_item_edit, viewGroup, false)
        return ViewHolder(view, mCallback)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val app = mLocalDataSet!![position]
        viewHolder.createHolder(app)
    }

    override fun getItemCount(): Int {
        return mLocalDataSet?.size ?: 0
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View, private val mCallback: OnAdapterListener) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private val iconView: ImageView
        private val titleView: TextView
        private val warningView: TextView
        private val limitButton: LinearLayout
        private val limitImage: ImageView
        private val removeButton: ImageView
        private var mApp: App? = null
        private var mPrevClickedID = -1
        fun createHolder(app: App) {
            mApp = app
            iconView.setImageBitmap(app.icon)
            titleView.text = app.label
            limitButton.setOnClickListener(this)
            removeButton.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            // undo
            if (mPrevClickedID == view.id) {
                limitImage.setColorFilter(Color.BLACK)
                limitButton.alpha = 1.0f
                removeButton.setColorFilter(Color.BLACK)
                removeButton.alpha = 1.0f
                mCallback.undoApp(mApp)
                mPrevClickedID = -1
                return
            }
            when (view.id) {
                R.id.button_edit_limit -> {
                    limitImage.setColorFilter(Color.RED)
                    limitButton.alpha = 1.0f
                    removeButton.setColorFilter(Color.BLACK)
                    removeButton.alpha = 0.5f
                    mCallback.limitApp(mApp)
                }
                R.id.button_edit_remove -> {
                    limitImage.setColorFilter(Color.BLACK)
                    limitButton.alpha = 0.5f
                    removeButton.setColorFilter(Color.RED)
                    removeButton.alpha = 1.0f
                    mCallback.removeApp(mApp)
                }
            }
            mPrevClickedID = view.id
        }

        init {
            iconView = view.findViewById(R.id.image_edit_icon)
            titleView = view.findViewById(R.id.text_edit_title)
            warningView = view.findViewById(R.id.text_edit_warning)
            limitButton = view.findViewById(R.id.button_edit_limit)
            limitImage = view.findViewById(R.id.image_edit_limit)
            removeButton = view.findViewById(R.id.button_edit_remove)
        }
    }

    interface OnAdapterListener {
        fun undoApp(app: App?)
        fun limitApp(app: App?)
        fun removeApp(app: App?)
        fun displayWarning(value: Boolean)
    }
}