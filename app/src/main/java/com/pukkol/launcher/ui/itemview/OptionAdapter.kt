package com.pukkol.launcher.ui.itemview

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.items.AbstractItem
import com.pukkol.launcher.R
import kotlinx.android.synthetic.main.item_popup_icon_label.view.*

class OptionAdapter(private val mLabelRes: Int, private val mIconRes: Int) : AbstractItem<OptionAdapter?, OptionAdapter.ViewHolder>() {

    override fun bindView(holder: ViewHolder, payloads: List<Any>) {
        super.bindView(holder, payloads)
        holder.labelView.setText(mLabelRes)
        holder.iconView.setImageResource(mIconRes)
    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        holder.labelView.text = null
        holder.iconView.setImageDrawable(null)
    }

    override fun getViewHolder(view: View): ViewHolder {
        return ViewHolder(view)
    }

    override fun getType(): Int {
        return R.id.id_adapter_popup_icon_label_item
    }

    override fun getLayoutRes(): Int {
        return R.layout.item_popup_icon_label
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var iconView: ImageView = itemView.item_popup_icon
        var labelView: TextView = itemView.item_popup_label
    }

}