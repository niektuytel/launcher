package com.pukkol.launcher.data.local.db

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.pukkol.launcher.data.model.Item
import com.pukkol.launcher.ui.home.desktop.CellContainer
import com.pukkol.launcher.ui.home.desktop.DesktopFragment
import com.pukkol.launcher.ui.home.desktop.DesktopLayout
import com.pukkol.launcher.ui.itemview.ItemAppView

/**
 * get/set data of the item Model to SQLite storage (@DataBaseHelper)
 * @Author Niek Tuytel (Okido)
 */
class DBItemHelper(context: Context?) : DBItemProfile() {
    private val mDb: DataBaseHelper?
    fun close() {
        mDb?.close()
    }

    /**
     * Methods
     */
    fun create(item: Item) {
        // item will always be visible when first added
        val success = mDb!!.insert(Table.TABLE_NAME, DBItemProfile.Companion.getCreateValues(item))
        Log.d(TAG, "createItem: " + item.label + " (ID: " + item.ID.toString() + ") succeeded:" + success)
    }

    fun create(item: Item, page: Int, itemPosition: Item.Location) {
        // item will always be visible when first added
        val success = mDb!!.insert(Table.TABLE_NAME, DBItemProfile.Companion.getCreateValues(item, page, itemPosition))
        Log.d(TAG, "createItem: " + item.label + " (ID: " + item.ID.toString() + ") succeeded:" + success)
    }

    fun update(item: Item) {
        val id = item.ID.toString()
        val success = mDb!!.update(Table.TABLE_NAME, DBItemProfile.Companion.getUpdateValues(item), Table.COLUMN_TIME, id)
        Log.d(TAG, "updateItem: " + item.label + " (ID: " + item.ID.toString() + ") succeeded:" + success)
    }

    fun update(item: ItemAppView) {
        delete(item)
        create(item)
    }

    fun update(item: Item, page: Int, itemPosition: Item.Location) {
        delete(item)
        create(item, page, itemPosition)
    }

    fun delete(item: Item) {
        // delete the item itself
        val success = mDb!!.delete(Table.TABLE_NAME, Table.COLUMN_TIME, item.ID.toString())
        Log.d(TAG, "deleteItem: " + item.label + " (ID: " + item.ID.toString() + ") succeeded:" + success)
    }

    fun save(item: Item?) {
        if (item == null) return
        val find = mDb!!.find(Table.TABLE_NAME, Table.COLUMN_TIME, item.ID.toString())
        if (find) {
            update(item)
        } else {
            create(item)
        }
    }

    fun save(item: Item, page: Int, itemPosition: Item.Location) {
        val find = mDb!!.find(Table.TABLE_NAME, Table.COLUMN_TIME, item.ID.toString())
        if (find) {
            update(item, page, itemPosition)
        } else {
            create(item, page, itemPosition)
        }
    }

    fun getDockItems(desktopFragment: DesktopFragment?, page: Any?): List<ItemAppView?>? {
        return mDb!!.getDock(desktopFragment, page)
    }

    fun getDesktopItems(desktopFragment: DesktopFragment?, desktopLayout: DesktopLayout, layoutParams: ViewGroup.LayoutParams?): ArrayList<CellContainer> {
        return mDb!!.getDesktop(desktopFragment, desktopLayout, layoutParams) as ArrayList<CellContainer>
    }

    companion object {
        private val TAG = DBItemHelper::class.java.simpleName
    }

    init {
        mDb = DataBaseHelper.Companion.getInstance(context)
    }
}