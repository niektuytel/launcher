package com.pukkol.launcher.data.local.db

import android.content.ContentValues
import android.content.Intent
import com.pukkol.launcher.data.model.Item
import com.pukkol.launcher.util.Definitions
import com.pukkol.launcher.util.Tool

open class DBItemProfile {
    object Table {
        const val TABLE_NAME = "home"
        const val COLUMN_TIME = "time"
        const val COLUMN_TYPE = "type"
        const val COLUMN_LOCATION = "location"
        const val COLUMN_LABEL = "label"
        const val COLUMN_PARENT_X = "parent_x"
        const val COLUMN_PARENT_Y = "parent_y"
        const val COLUMN_CHILD_X = "child_x"
        const val COLUMN_CHILD_Y = "child_y"
        const val COLUMN_PARENT_WIDTH = "parent_width"
        const val COLUMN_PARENT_HEIGHT = "parent_height"
        const val COLUMN_CHILD_WIDTH = "child_width"
        const val COLUMN_CHILD_HEIGHT = "child_height"
        const val COLUMN_PAGE = "page"
        const val COLUMN_DESKTOP = "desktop"
        const val COLUMN_LIMIT = "limitation"
        const val COLUMN_EXTRA = "data"
        const val CREATE = ("CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_TIME + " INTEGER PRIMARY KEY,"
                + COLUMN_TYPE + " VARCHAR,"
                + COLUMN_LOCATION + " VARCHAR,"
                + COLUMN_LABEL + " VARCHAR,"
                + COLUMN_PARENT_X + " INTEGER,"
                + COLUMN_PARENT_Y + " INTEGER,"
                + COLUMN_CHILD_X + " INTEGER,"
                + COLUMN_CHILD_Y + " INTEGER,"
                + COLUMN_PARENT_WIDTH + " INTEGER,"
                + COLUMN_PARENT_HEIGHT + " INTEGER,"
                + COLUMN_CHILD_WIDTH + " INTEGER,"
                + COLUMN_CHILD_HEIGHT + " INTEGER,"
                + COLUMN_PAGE + " INTEGER,"
                + COLUMN_DESKTOP + " INTEGER,"
                + COLUMN_LIMIT + " TEXT,"
                + COLUMN_EXTRA + " VARCHAR"
                + ")")
    }

    companion object {
        fun getCreateValues(item: Item): ContentValues {
            val itemValues = getUpdateValues(item)
            itemValues.put(Table.COLUMN_TIME, item.ID)
            itemValues.put(Table.COLUMN_TYPE, item.type.toString())
            itemValues.put(Table.COLUMN_LOCATION, item.location.toString())
            itemValues.put(Table.COLUMN_PAGE, item.pageIndex)
            itemValues.put(Table.COLUMN_DESKTOP, item.location!!.ordinal)
            return itemValues
        }

        fun getCreateValues(item: Item, page: Int, itemPosition: Item.Location): ContentValues {
            val itemValues = getUpdateValues(item)
            itemValues.put(Table.COLUMN_TIME, item.ID)
            itemValues.put(Table.COLUMN_TYPE, item.type.toString())
            itemValues.put(Table.COLUMN_LOCATION, item.location.toString())
            itemValues.put(Table.COLUMN_PAGE, page)
            itemValues.put(Table.COLUMN_DESKTOP, itemPosition.ordinal)
            return itemValues
        }

        fun getUpdateValues(item: Item): ContentValues {
            val itemValues = ContentValues()
            itemValues.put(Table.COLUMN_LABEL, item.label)
            itemValues.put(Table.COLUMN_PARENT_X, item.parentBorder.left)
            itemValues.put(Table.COLUMN_PARENT_Y, item.parentBorder.top)
            itemValues.put(Table.COLUMN_CHILD_X, item.childBorder.left)
            itemValues.put(Table.COLUMN_CHILD_Y, item.childBorder.top)
            itemValues.put(Table.COLUMN_PARENT_WIDTH, item.parentBorder.width())
            itemValues.put(Table.COLUMN_PARENT_HEIGHT, item.parentBorder.height())
            itemValues.put(Table.COLUMN_CHILD_WIDTH, item.childBorder.width())
            itemValues.put(Table.COLUMN_CHILD_HEIGHT, item.childBorder.height())
            itemValues.put(Table.COLUMN_LOCATION, item.location.toString())
            val concat = StringBuilder()
            var intentApp: Intent?
            var intentString: String?
            when (item.type) {
                Item.Type.APP, Item.Type.SHORTCUT -> {
                    intentApp = item.app?.let { Tool.getIntentFromApp(it) }
                    intentString = Tool.getIntentAsString(intentApp)
                    itemValues.put(Table.COLUMN_EXTRA, intentString)
                }
                Item.Type.FOLDER -> {
                    for (app in item.folderApps!!) {
                        intentApp = Tool.getIntentFromApp(app)
                        intentString = Tool.getIntentAsString(intentApp)
                        concat.append(intentString)
                        concat.append(Definitions.DELIMITER)
                    }
                    itemValues.put(Table.COLUMN_EXTRA, concat.toString())
                }
            }
            return itemValues
        }
    }
}