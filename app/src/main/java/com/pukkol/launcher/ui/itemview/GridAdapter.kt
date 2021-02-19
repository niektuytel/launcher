package com.pukkol.launcher.ui.itemview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.pukkol.launcher.R
import com.pukkol.launcher.data.model.App
import com.pukkol.launcher.data.model.Item
import com.pukkol.launcher.ui.home.desktop.DesktopFragment
import com.pukkol.launcher.ui.itemview.ItemAppView.OnActionListener
import kotlinx.android.synthetic.main.view_menu_adapter.view.*

/**
 * @author okido
 * @since 1/22/2021
 *
 * adapter for gridlayouts
 */
class GridAdapter(private val mLocation: Item.Location, var desktopFragment: DesktopFragment) : RecyclerView.Adapter<GridAdapter.ViewHolder>() {
    private var apps = ArrayList<App>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_menu_adapter, parent, false)
        return ViewHolder(desktopFragment, view, mLocation)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.createGrid(
                apps[position]
        )
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return apps.size
    }

    fun updateData(items: ArrayList<App>) {
        apps = items
        notifyDataSetChanged()
    }

    class ViewHolder(var desktopFragment: DesktopFragment, resourceView: View?, var location: Item.Location) : RecyclerView.ViewHolder(resourceView!!), OnActionListener {
        private val mLayout: LinearLayout = itemView.frame_menu_adapter_layout

        fun createGrid(app: App) {
            val item = ItemAppView(
                    desktopFragment,
                    app.label,
                    mLayout,
                    app,
                    location
            ).withListener(this)

            // remove all views (search freeze view issue)
            mLayout.removeAllViews()
            mLayout.addView(item)
        }

        override fun replaceOldView(app: App) {
            createGrid(app)
        }
    }
}