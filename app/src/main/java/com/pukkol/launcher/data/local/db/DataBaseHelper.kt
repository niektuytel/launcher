package com.pukkol.launcher.data.local.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.view.ViewGroup
import com.pukkol.launcher.data.model.Item
import com.pukkol.launcher.ui.home.desktop.CellContainer
import com.pukkol.launcher.ui.home.desktop.DesktopFragment
import com.pukkol.launcher.ui.home.desktop.DesktopLayout
import com.pukkol.launcher.ui.itemview.ItemAppView
import com.pukkol.launcher.ui.itemview.ItemWidgetView
import java.util.*

/**
 * get/set data from SQLite storage
 * @Author Niek Tuytel (Okido)
 */
class DataBaseHelper(private val mContext: Context?) : SQLiteOpenHelper(mContext, DATABASE_NAME, null, DATABASE_VERSION) {
    private val mDb: SQLiteDatabase?
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        // Uncomment line below if you want to enable foreign keys
        // dbExecSQL("PRAGMA foreign_keys");
    }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        if (sqLiteDatabase == null) return
        sqLiteDatabase.beginTransaction()
        try {
            sqLiteDatabase.execSQL(DBItemProfile.Table.CREATE)
            //Add other tables here
            sqLiteDatabase.setTransactionSuccessful()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        // discard the data and start over
        mDb!!.execSQL(SQL_DELETE + DBItemProfile.Table.TABLE_NAME)
        //Add other tables here
    }

    fun onUpgrade(i: Int, i1: Int) {
        mDb?.let { onUpgrade(it, i, i1) }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    override fun close() {
        if (mDb != null) {
            mDb.close()
            super.close()
        }
    }

    /**
     * Query Methods
     */
    fun insert(tableName: String?, cv: ContentValues?): Boolean {
        val response = mDb!!.insert(tableName, null, cv)
        return response != -1L // -1 = error
    }

    fun update(tableName: String?, cv: ContentValues?, whereName: String?, isValue: String): Boolean {
        val result = mDb!!.update(tableName, cv, "$whereName = ? ", arrayOf(isValue))
        return result != 0
    }

    fun delete(tableName: String?, whereName: String?, isValue: String): Boolean {
        val result = mDb!!.delete(tableName, "$whereName = ? ", arrayOf(isValue))
        return result != 0
    }

    fun find(tableName: String?, whereName: String?, isValue: String): Boolean {
        val query = "SELECT * FROM $tableName WHERE $whereName = '$isValue'"
        val cursor = mDb!!.rawQuery(query, null)
        val result = cursor.count > 0

        // close
        cursor.close()
        return result
    }

    fun getDock(desktopFragment: DesktopFragment?, page: Any?): List<ItemAppView> {
        val SQL_QUERY_DESKTOP = "SELECT * FROM " + DBItemProfile.Table.TABLE_NAME
        val cursor = mDb!!.rawQuery(SQL_QUERY_DESKTOP, null)
        val dock: MutableList<ItemAppView> = ArrayList()
        if (cursor.moveToFirst()) {
            val desktopColumnIndex = cursor.getColumnIndex(DBItemProfile.Table.COLUMN_DESKTOP)
            do {
                val desktopVar = cursor.getString(desktopColumnIndex).toInt()
                if (desktopVar == Item.Location.DOCK.ordinal) {
                    dock.add(ItemAppView(desktopFragment!!, cursor, page))
                }
            } while (cursor.moveToNext())
        }

        // close
        cursor.close()
        return dock
    }

    fun getDesktop(desktopFragment: DesktopFragment?, desktopLayout: DesktopLayout, layoutParams: ViewGroup.LayoutParams?): List<CellContainer> {
        val SQL_QUERY_DESKTOP = "SELECT * FROM " + DBItemProfile.Table.TABLE_NAME
        val cursor = mDb!!.rawQuery(SQL_QUERY_DESKTOP, null)
        val pages: MutableList<CellContainer> = ArrayList()
        if (cursor.moveToFirst()) {
            val pageColumnIndex = cursor.getColumnIndex(DBItemProfile.Table.COLUMN_PAGE)
            val desktopColumnIndex = cursor.getColumnIndex(DBItemProfile.Table.COLUMN_DESKTOP)
            do {
                val pageIndex = cursor.getString(pageColumnIndex).toInt()
                val desktopVar = cursor.getString(desktopColumnIndex).toInt()

                // create item on page
                if (desktopVar == Item.Location.DESKTOP.ordinal) {
                    while (pageIndex >= pages.size) {
                        pages.add(desktopLayout.onCreatePage(layoutParams!!))
                    }
                    val columnType = cursor.getString(cursor.getColumnIndex(DBItemProfile.Table.COLUMN_TYPE))
                    val type = Item.Type.valueOf(columnType)
                    var item: Item
                    item = if (type == Item.Type.WIDGET) {
                        ItemWidgetView(desktopFragment!!, cursor, pages[pageIndex])
                    } else {
                        ItemAppView(desktopFragment!!, cursor, pages[pageIndex])
                    }

                    // not possible to set to page, remove
                    run {
                        val page = pages[pageIndex]
                        val border = item.parentBorder
                        val error = page.getItem(border) != null && page.isEmptySpan(border)
                        if (error) {
                            page.removeItem(item)
                        } else {
                            page.setItem(item)
                        }
                    }
                }
            } while (cursor.moveToNext())
        }

        // minimal 1 page
        if (pages.size == 0) {
            pages.add(desktopLayout.onCreatePage(layoutParams!!))
        }

        // close
        cursor.close()
        return pages
    }

    companion object {
        const val SQL_DELETE = "DROP TABLE IF EXISTS "
        const val DATABASE_NAME = "launcher.db"
        var DATABASE_VERSION = 1
        private var sInstance: DataBaseHelper? = null

        /**
         * Getters
         */
        fun getInstance(context: Context?): DataBaseHelper? {
            // Use the application context, which will ensure that you
            // don't accidentally leak an Activity's context.
            // See this article for more information: http://bit.ly/6LRzfx
            if (sInstance == null) {
                sInstance = DataBaseHelper(context)
            }
            return sInstance
        }
    }

    init {
        mDb = this.writableDatabase
    }
}