package com.pukkol.launcher.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ShortcutIconResource
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Parcelable
import android.util.Log
import android.util.Size
import android.widget.LinearLayout
import com.pukkol.launcher.R
import com.pukkol.launcher.Setup
import com.pukkol.launcher.data.model.Item
import com.pukkol.launcher.ui.home.desktop.CellContainer
import com.pukkol.launcher.ui.home.desktop.DesktopFragment
import com.pukkol.launcher.ui.home.desktop.DockView
import com.pukkol.launcher.ui.itemview.ItemAppView
import com.pukkol.launcher.util.Display
import com.pukkol.launcher.util.Tool

class ShortcutReceiver(private val mDesktopFragment: DesktopFragment) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.extras == null) return

        // this will only work before Android Oreo
        // was deprecated in favor of ShortcutManager.pinRequestShortcut()
        val shortcutLabel = intent.extras!!.getString(Intent.EXTRA_SHORTCUT_NAME)
        val shortcutIntent = intent.extras!![Intent.EXTRA_SHORTCUT_INTENT] as Intent?
        var shortcutIcon: Bitmap? = null
        try {
            val parcelable = intent.extras!!.getParcelable<Parcelable>(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)
            if (parcelable is ShortcutIconResource) {
                val iconResource = parcelable
                val resources = context.packageManager.getResourcesForApplication(iconResource.packageName)
                if (resources != null) {
                    val id = resources.getIdentifier(iconResource.resourceName, null, null)
                    shortcutIcon = Tool.drawableToBitmap(resources.getDrawable(id))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (shortcutIcon == null) {
                shortcutIcon = Tool.drawableToBitmap(
                        BitmapDrawable(
                                context.resources,
                                intent.extras!!.getParcelable<Parcelable>(Intent.EXTRA_SHORTCUT_ICON) as Bitmap?
                        )
                )
            }
        }
        val preferredPos = mDesktopFragment.pagerView?.currentPage!!.getEmptySpan(Size(1, 1))
        val app = Setup.appManager().createApp(shortcutIntent)
        app!!.icon = shortcutIcon!!

//        ItemAppView item = new ItemAppView(mDesktopFragment, null);
//        item.setApp(app);
        val item = ItemAppView(
                mDesktopFragment,
                shortcutLabel!!,
                mDesktopFragment.pagerView!!.currentPage!!,
                app,
                currentLocation(mDesktopFragment.pagerView!!.currentPage)
        )


//        if (app != null) {
//            item.setLocation(DESKTOP);
//            ItemViewManager.setItemAsApp(item, item);
//        } else {
//            item = ItemViewManager.newShortcutItem(context, shortcutIntent, shortcutIcon, shortcutLabel, mDesktopFragment.getPagerView().getCurrentPage(), mDesktopFragment);
//        }
        if (preferredPos == null) {
            Display.toast(mDesktopFragment.pagerView!!.context, R.string.toast_not_enough_space)
        } else {
            // item.setPositionParent(preferredPos.x, preferredPos.y);
            mDesktopFragment.pagerView!!.currentPage!!.setItem(item)
            Setup.itemManager().save(item)

            //HomeActivity.dbItem.save(item, mDesktopFragment.getPagerView().getCurrentItem(), DESKTOP);
            Log.d(this.javaClass.toString(), "shortcut installed")
        }
    }

    private fun currentLocation(page: Any?): Item.Location {
        // Dock view
        if (page is DockView) {
            return Item.Location.DOCK
        } else if (page is CellContainer) {
            return Item.Location.DESKTOP
        } else if (page is LinearLayout) {
            return Item.Location.FOLDER
        }
        return Item.Location.DESKTOP
    }

    companion object {
        private val TAG = ShortcutReceiver::class.java.simpleName
    }
}