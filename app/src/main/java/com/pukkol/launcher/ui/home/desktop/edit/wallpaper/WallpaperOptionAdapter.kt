package com.pukkol.launcher.ui.home.desktop.edit.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pukkol.launcher.R
import com.pukkol.launcher.ui.home.desktop.edit.wallpaper.WallpaperOptionAdapter.ViewHolder.onClickListener
import java.util.*

class WallpaperOptionAdapter(private val mContext: Context, private val mListener: onClickListener) : RecyclerView.Adapter<WallpaperOptionAdapter.ViewHolder>() {
    private var mItems: List<WallpaperOptionContainer>? = null
    private var mLatestSelected = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_wallpaper, parent, false)
        return ViewHolder(mContext, view, mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = mItems!![position]

        // set size
        if (model.labelSize.width != -1 && model.labelSize.height != -1) {
            if (model.bitmap == null) {
                // pick image
                holder.itemView.layoutParams.width = model.labelSize.width
                holder.itemView.layoutParams.height = model.labelSize.height
                holder.textPickImage.visibility = View.VISIBLE
            } else {
                // set wallpaper
                holder.imageIcon.layoutParams.width = model.labelSize.width
                holder.imageIcon.layoutParams.height = model.labelSize.height
                holder.imageIcon.setImageBitmap(model.label)
                holder.setDone(model.isCurrentWallpaper)
            }
            holder.bind(model, position)
        }
    }

    override fun getItemCount(): Int {
        return if (mItems == null) 0 else mItems!!.size
    }

    var items: List<WallpaperOptionContainer>?
        get() = if (mItems == null) {
            ArrayList()
        } else mItems
        set(wallpaperOptionContainers) {
            mItems = wallpaperOptionContainers
        }
    val currentWallpaper: Bitmap?
        get() = mItems!![mLatestSelected].bitmap

    fun updateUsages(position: Int, removeAll: Boolean) {
        if (mLatestSelected == position) return
        val items = items!!
        if (mLatestSelected != -1) {
            val item = items[mLatestSelected]
            item.isCurrentWallpaper(false)
            notifyItemChanged(mLatestSelected, item)
        }
        if (!removeAll) {
            val item = items[position]
            item.isCurrentWallpaper(true)
            notifyItemChanged(position, item)
            mLatestSelected = position
        }
    }

    class ViewHolder(private val mContext: Context, itemView: View, private val mListener: onClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var imageIcon: ImageView
        var textPickImage: TextView
        private var mDataModel: WallpaperOptionContainer? = null
        private var mPosition = 0
        fun bind(dataModel: WallpaperOptionContainer?, position: Int) {
            mDataModel = dataModel
            mPosition = position
        }

        fun setDone(done: Boolean) {
            if (done) {
                val drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_check_white_gray)
                imageIcon.foreground = drawable
            } else {
                imageIcon.foreground = null
            }
        }

        override fun onClick(view: View) {
            mListener.onItemClickListener(mDataModel, mPosition)
        }

        interface onClickListener {
            fun onItemClickListener(item: WallpaperOptionContainer?, position: Int)
        }

        init {
            imageIcon = itemView.findViewById(R.id.image_label_wallpaper)
            textPickImage = itemView.findViewById(R.id.text_bmp_pick_image)
            itemView.setOnClickListener(this)
        }
    }
}