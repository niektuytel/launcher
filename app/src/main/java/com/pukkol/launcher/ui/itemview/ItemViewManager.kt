package com.pukkol.launcher.ui.itemview

import android.widget.LinearLayout
import com.pukkol.launcher.R
import com.pukkol.launcher.Setup
import com.pukkol.launcher.data.model.App
import com.pukkol.launcher.data.model.Item
import com.pukkol.launcher.ui.home.desktop.CellContainer
import com.pukkol.launcher.ui.home.desktop.DesktopFragment
import com.pukkol.launcher.ui.home.desktop.DockView
import com.pukkol.launcher.util.Display
import kotlin.collections.ArrayList

/**
 * @author okido (Niek Tuytel)[2/12/2021]
 * set Item to a new Item perspective and
 * display get as well changed
 */
object ItemViewManager {
    fun setInDesktop(desktopFragment: DesktopFragment?, fingerItem: Any) {
        val item = fingerItem as Item
        val page = item.pageCurrentEvent as CellContainer?
        val cellItem = page!!.getItem(item.parentBorder)

        // Collide -> try set in Folder
        if (item.type !== Item.Type.WIDGET) {
            if (cellItem != null) {
                if (setInFolder(desktopFragment!!, cellItem, item)) {
                    return
                } else {
                    Display.toast(item.context, R.string.toast_not_enough_space)
                }
            }
        }
        page.setItem(item)
        Setup.itemManager().save(item)
    }

    private fun setInFolder(desktopFragment: DesktopFragment, desktopItem: Item?, fingerItem: Item): Boolean {
        if (desktopItem == null) return false
        val page = fingerItem.pageCurrentEvent as CellContainer?
        var folderApps: ArrayList<App>? = ArrayList()
        var folderLabel = ""
        when (desktopItem.type) {
            Item.Type.APP, Item.Type.SHORTCUT -> {
                when (fingerItem.type) {
                    Item.Type.APP, Item.Type.SHORTCUT -> {

                        // data
                        folderApps!!.add(desktopItem.app!!)
                        folderApps.add(fingerItem.app!!)
                        folderLabel = ""
                    }
                    Item.Type.FOLDER -> {
                        val total = fingerItem.folderApps!!.size + 1
                        if (total > Item.MAX_FOLDER_INDEX) return false

                        // data
                        folderApps!!.add(desktopItem.app!!)
                        folderApps.addAll(fingerItem.folderApps!!)
                        folderLabel = fingerItem.label
                    }
                }
            }
            Item.Type.FOLDER -> {
                when (fingerItem.type) {
                    Item.Type.APP, Item.Type.SHORTCUT -> {
                        val total = desktopItem.folderApps!!.size + 1
                        if (total > Item.MAX_FOLDER_INDEX) return false

                        // data
                        folderApps = desktopItem.folderApps!!
                        folderApps.add(fingerItem.app!!)
                        folderLabel = desktopItem.label
                    }
                    Item.Type.FOLDER -> {
                        val total = desktopItem.folderApps!!.size + fingerItem.folderApps!!.size
                        if (total > Item.MAX_FOLDER_INDEX) return false

                        // data
                        folderApps = desktopItem.folderApps!!
                        folderApps.addAll(fingerItem.folderApps!!)
                        folderLabel = desktopItem.label
                    }
                }
            }
        }

        // new folder item
        return if (folderApps!!.size > 1) {
            desktopItem.folderApps = folderApps
            val folderItem = ItemAppView(
                    desktopFragment,
                    folderLabel,
                    fingerItem.pageCurrentEvent!!,
                    desktopItem,
                    getLocation(fingerItem.pageCurrentEvent)
            )

            // storage
            Setup.itemManager().delete(fingerItem)

            // update page view
            page!!.removeItem(desktopItem)
            // Setup.itemManager().delete(desktopItem);
            page.setItem(folderItem)
            Setup.itemManager().save(folderItem)
            true
        } else {
            false
        }
    }

    fun setOutFolder(
            desktopFragment: DesktopFragment?,
            desktopItem: ItemAppView?,
            fingerItem: ItemAppView
    ) {
        var desktopItem = desktopItem ?: return

        // create
        val page = desktopFragment!!.touchView!!.dragHelper.currentPage
        desktopItem.removeFolderItem(fingerItem.app)

        // data
        val pageIndex: Int
        var cellContainer: CellContainer? = desktopFragment.dockView!!
        if (desktopItem.location === Item.Location.DESKTOP) {
            pageIndex = desktopFragment.pagerView!!.currentItem
            cellContainer = desktopFragment.pagerView!!.pages[pageIndex]
        }

        // update desktop item
        cellContainer!!.removeItem(desktopItem) // remove view
        if (desktopItem.folderApps!!.size == 1) {
            // folder into app item
            val oldFolder = desktopItem
            desktopItem = ItemAppView(
                    desktopFragment!!,
                    oldFolder.folderApps!![0].label,
                    page!!,
                    oldFolder,
                    getLocation(page)
            )
        } else {
            desktopItem.folderApps = desktopItem.folderApps
            desktopItem.type = Item.Type.FOLDER
        }
        cellContainer.setItem(desktopItem) // add view

        // update finger item
        run {
            fingerItem.removeItemView()
            fingerItem.setPageCurrentEvent(page, desktopItem.location)
            fingerItem.childSize = fingerItem.cellPxSize
        }

        // storage
        Setup.itemManager().save(desktopItem)
        Setup.itemManager().save(fingerItem)
    }

    private fun getLocation(page: Any?): Item.Location {
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
}