package com.pukkol.launcher.ui.itemview

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.database.Cursor
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Size
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import com.pukkol.launcher.BuildConfig
import com.pukkol.launcher.R
import com.pukkol.launcher.Setup
import com.pukkol.launcher.data.local.db.DBItemProfile
import com.pukkol.launcher.data.model.App
import com.pukkol.launcher.data.model.Item
import com.pukkol.launcher.interfaces.INotificationListener
import com.pukkol.launcher.services.NotificationService.Companion.setNotificationCallback
import com.pukkol.launcher.ui.home.HomeActivity
import com.pukkol.launcher.ui.home.desktop.CellContainer
import com.pukkol.launcher.ui.home.desktop.CellParams
import com.pukkol.launcher.ui.home.desktop.DesktopFragment
import com.pukkol.launcher.util.Definitions
import com.pukkol.launcher.util.Display
import com.pukkol.launcher.util.Tool
import com.pukkol.launcher.viewutil.ImageView
import com.pukkol.launcher.viewutil.TextView
import kotlinx.android.synthetic.main.view_app.view.*
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("ViewConstructor")
@Suppress("UNCHECKED_CAST", "DIFFERENT_NAMES_FOR_THE_SAME_PARAMETER_IN_SUPERTYPES")
class ItemAppView : Item, INotificationListener, Drawable.Callback, View.OnTouchListener, View.OnLongClickListener {

    var layout: LinearLayout
        private set
    private var textNotification: TextView
    private var imageIcon: ImageView
    private var textLabel: TextView

    private var mDesktopFragment: DesktopFragment

    private var startingPoint = Point(0, 0)
    private var mListener: OnActionListener? = null
    private var notificationCount = 0
    private var isDragging = false

    constructor(
            desktopFragment: DesktopFragment,
            label: String,
            layout: Any,
            itemOrApp_s: Any,
            location: Location
    ) : super(desktopFragment.requireContext()) {
        mDesktopFragment = desktopFragment

        // layout data
        setPageCurrentEvent(layout, location)
        this.parentPosition = getParentPosition(itemOrApp_s, layout)
        this.childPosition = Point(0, 0)
        this.parentSize = Size(1, 1)
        this.childSize = cellPxSize

        // item data
        resetID()
        this.app = getApp(itemOrApp_s)
        this.type = getType(itemOrApp_s)
        this.folderApps = getApps(itemOrApp_s) as ArrayList<App>?
        this.labelColor = getLabelColor(location)
        this.labelVisible = getLabelVisibility(location)

        // set view
        setIconView(app!!.icon)
        setLabelView(label)
    }

    constructor(desktopFragment: DesktopFragment, cursor: Cursor, layout: Any?) : super(desktopFragment, cursor, layout) {
        mDesktopFragment = desktopFragment

        // item data
        val extra = cursor.getString(cursor.getColumnIndex(DBItemProfile.Table.COLUMN_EXTRA))
        val type = type!!
        var intent: Intent?
        when (type) {
            Type.APP, Type.SHORTCUT -> {
                intent = Tool.getIntentFromString(extra)
                app = Setup.appManager().findApp(intent)

                // set view
                setIconView(app!!.icon)
                setLabelView(label)
            }
            Type.FOLDER -> {
                folderApps = ArrayList()
                val dataSplit = extra.split(Definitions.DELIMITER.toRegex()).toTypedArray()
                for (splitData in dataSplit) {
                    if (splitData.isEmpty()) continue
                    intent = Tool.getIntentFromString(splitData)
                    val app = Setup.appManager().findApp(intent)
                    folderApps!!.add(app!!)
                }

                // item
                setDrawableIcon(
                        FolderDrawable(this.context, this)
                )

                // set view
                setIconView(bitmapIcon!!)
                setLabelView(label)
            }
            else -> return
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_app, this)
        layout = app_layout_view
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        imageIcon = image_icon
        textLabel = text_label
        textNotification = text_notification

        setOnLongClickListener(this)
        setOnTouchListener(this)
    }

    override val currentPage: Any?
        get() = mDesktopFragment.touchView!!.dragHelper.currentPage

    override val pageIndex: Int
        get() = mDesktopFragment.touchView!!.dragHelper.currentPageIndex

//    override fun onInterceptTouchEvent(motionEvent: MotionEvent?): Boolean {
//        // set on Touch
//        return mDesktopFragment.touchView!!.onTouch(this, motionEvent) || super.onInterceptTouchEvent(motionEvent)/* || mDesktopFragment.touchView!!.onTouch(this, ev)*/
//
//        // /*mDesktopFragment.touchView!!.onInterceptTouchEvent(ev) ||*/
//    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        if (motionEvent.action == MotionEvent.ACTION_DOWN) {
            startingPoint = Point(
                    motionEvent.x.toInt(),
                    motionEvent.y.toInt()
            )
        }

        // onClick
        if (motionEvent.action == MotionEvent.ACTION_UP && !isDragging) {
            openItem(/*motionEvent*/)
        }

        return mDesktopFragment.touchView!!.onTouch(view, motionEvent) || super.onTouchEvent(motionEvent)
//        // onDrag
//        isDragging = mDesktopFragment.touchView!!.onTouch(view, motionEvent)
//        return isDragging
//        return false
    }

    override fun onLongClick(view: View): Boolean {
        if (!isDragging) {

            // set new item
            if (mListener != null && location === Location.MENU) {
                mListener!!.replaceOldView(app!!)
            }

            mDesktopFragment.touchView!!.onStartDragItem(this)
            return true
        }
        return false
    }

    override fun setItemView() {
        val size = cellPxSize!!
        val params = CellParams(
                size.width,
                size.height,
                parentPosition!!,
                parentSize!!
        )
        layoutParams = params
        if (type === Type.FOLDER) {
            setDrawableIcon(
                    FolderDrawable(this.context, this)
            )
        }

        // data
        tag = this
        when (type) {
            Type.APP -> {
                val hasNotification = Setup.deviceSettings().badgeNotification
                if (app != null && hasNotification) {
                    val packageName = app!!.packageName
                    setNotificationCallback(packageName, this)
                }
            }
            Type.FOLDER -> {
                setLayerType(LAYER_TYPE_SOFTWARE, null)
            }
            else -> return
        }
    }

    override fun updateView() {
        val oldSize = childSize
        val newSize = cellPxSize!!
        childSize = newSize

        // update item
        if (oldSize != null) {
            labelVisible = location !== Location.DOCK
            setLabelView(this.label)

            // padding item
            // TODO this way of doing is not to accurate the `paddingTop` is the wrong value
            // int paddingTop = this.getResources() .getDimensionPixelSize(R.dimen.app_padding_top);
            val difference = oldSize.height - newSize.height
            val iconTop = difference / 2
            layout.setPadding(0, iconTop, 0, 0)

            // set layout size
            layout(0, 0, newSize.width, newSize.height)
            layout.layout(0, 0, newSize.width, newSize.height)
            layout.invalidate()
            this.invalidate()
        }
    }

    override fun setNotificationView(count: Int) {
        notificationCount = count
        val value : String = if (count > 99) "++" else count.toString()
        if (count == 0) {
            textNotification.visibility = GONE
        } else {
            textNotification.visibility = VISIBLE
            textNotification.text = value
        }
        this.invalidate()
    }

    private fun setIconView(icon: Bitmap) {
        super.setBitmapIcon(icon)
        if (type === Type.WIDGET) {
            imageIcon.visibility = GONE
        } else {
            imageIcon.setImageBitmap(bitmapIcon)
        }

        this.invalidate()
    }

    private fun setLabelView(label: String) {
        if (type === Type.WIDGET) {
            textLabel.visibility = GONE
        } else if (!labelVisible) {
            textLabel.visibility = GONE
        } else {
            textLabel.visibility = VISIBLE
            textLabel.setTextColor(labelColor)
            textLabel.text = label
        }

        this.label = label
        this.invalidate()
    }

    fun resetID() {
        val random = Random()
        ID = random.nextInt()
    }

    private fun openItem(/*motionEvent: MotionEvent*/)/*: Boolean*/ {
        // val finger = Point(motionEvent.x.toInt(), motionEvent.y.toInt())
        // if (isOutsideThreshold(finger, startingPoint)) {
        if (type === Type.FOLDER) {
            // open folder
            mDesktopFragment.folderView!!.onFolderOpen(this)
        } else {
            // open app
            Tool.createScaleInScaleOutAnim(this) {
                val appName = app!!.packageName
                if (BuildConfig.APPLICATION_ID != appName) {
                    openApp()
                } else {
                    // own application
                    // open application with the same package name
                    openApp()
                }
            }
        }
//        }
        startingPoint = Point(0, 0)
//        return false
    }

    private fun openApp() {
        try {
            // open activity
            context.startActivity(Tool.getIntentFromApp(app!!), onRevealAnimation())

            // close app option menu
            HomeActivity.launcher!!.onBackPressed()
        } catch (e: Exception) {
            e.printStackTrace()
            Display.toast(context, R.string.toast_app_uninstalled)
        }
    }

    private fun onRevealAnimation(): Bundle {
        val border = parentBorder
        val options = ActivityOptions.makeClipRevealAnimation(
                this,
                border.left,
                border.top,
                border.right,
                border.bottom
        )
        return options.toBundle()
    }

    private fun getParentPosition(itemOrApp_s: Any, layout: Any?): Point? {
        // position item
        return when {
            itemOrApp_s is Item -> {
                itemOrApp_s.parentPosition
            }
            layout is CellContainer -> {
                layout.getEmptySpan(parentSize!!)
            }
            else -> {
                // no place contains
                Point(-1, -1)
            }
        }
    }

    private fun getApps(itemOrApp_s: Any): List<App>? {
        if (itemOrApp_s is Item) {
            if (itemOrApp_s.folderApps != null && itemOrApp_s.folderApps!!.size > 1) {
                return itemOrApp_s.folderApps
            }
        } else if (itemOrApp_s is List<*>) {
            val models = itemOrApp_s as List<App>
            if (models.size > 1) {
                return models
            }
        }
        return null
    }

    private fun getType(itemOrApp_s: Any): Type {
        var item: Item? = null
        if (itemOrApp_s is Item) {
            item = itemOrApp_s
        }

        return if (itemOrApp_s is ArrayList<*> || item?.folderApps != null && item.folderApps!!.size > 1) {
            Type.FOLDER
        } else if (itemOrApp_s is App || item != null) {
            Type.APP
        } else {
            try {
                throw Exception(
                        "apps instanceof " + itemOrApp_s.javaClass.simpleName + " not been found"
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Type.SHORTCUT
        }
    }

    private fun getApp(itemOrApp_s: Any): App? {
        if (itemOrApp_s is Item) {
            return if (itemOrApp_s.folderApps != null && itemOrApp_s.folderApps!!.size == 1) {
                itemOrApp_s.folderApps!![0]
            } else {
                itemOrApp_s.app
            }
        } else if (itemOrApp_s is App) {
            return itemOrApp_s
        } else if (itemOrApp_s is List<*>) {
            val apps = itemOrApp_s as List<App>
            if (apps.size == 1) {
                return apps[0]
            }
        } else {
            try {
                throw Exception(
                        "itemOrApp_s instanceof " + itemOrApp_s.javaClass.simpleName + " not been found"
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun getLabelColor(location: Location): Int {
        return if (location === Location.FOLDER) {
            Color.BLACK
        } else {
            Color.WHITE
        }
    }

    private fun getLabelVisibility(location: Location) :Boolean {
        return (location != Location.DOCK)
    }

    fun withListener(listener: OnActionListener?): ItemAppView {
        mListener = listener
        return this
    }

    interface OnActionListener {
        fun replaceOldView(app: App)
    }
}