package com.pukkol.launcher.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.listeners.OnClickListener
import com.pukkol.launcher.R
import com.pukkol.launcher.data.model.Item
import com.pukkol.launcher.ui.home.TouchDragHelper.DragMode
import com.pukkol.launcher.ui.home.desktop.CellContainer
import com.pukkol.launcher.ui.itemview.OptionAdapter
import com.pukkol.launcher.util.Display
import com.pukkol.launcher.util.Tool
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator
import java.util.*
import com.mikepenz.fastadapter.IAdapter as IAdapter1

/**
 * @author okido
 * @since 1/21/2021
 * <>
 * Detect Finger is on Layout:
 * boolean onTouchIntercept(Point finger)
 *
 * Hide Layout:
 * void onHideView()
 *
 * Show Layout:
 * void onShowView(ItemAppView item, Point finger)
 * >
 */
@SuppressLint("ViewConstructor")
class HomeOptionsView(private val mTouchView: HomeTouchView) : RecyclerView(mTouchView.context), OnClickListener<OptionAdapter> {
    private val uninstallItem = OptionAdapter(R.string.uninstall, R.drawable.ic_delete).withIdentifier(UNINSTALL.toLong())
    private val infoItem = OptionAdapter(R.string.info, R.drawable.ic_info).withIdentifier(INFO.toLong())
    private val removeItem = OptionAdapter(R.string.remove, R.drawable.ic_close).withIdentifier(REMOVE.toLong())

    private val slideInLeftAnimator: SlideInLeftAnimator
    private val slideInRightAnimator: SlideInRightAnimator
    private var mItem: Any? = null
    private val mAdapter = FastItemAdapter<OptionAdapter>()
    private val itemWidth: Int
    private val marginTop: Int

    private val marginHorizontal: Int
    private var show = false


    private val display = Display.DISPLAY_SIZE!!
    private val displayCenter: Point

    companion object {
        private val MARGIN_BORDER = Display.dp2px(10f)
        private const val UNINSTALL = 83
        private const val INFO = 84
        private const val REMOVE = 86
        private const val RESIZE = 87
    }

    init {
        displayCenter = Point(display.width / 2, display.height / 2)
        itemWidth = context.resources.getDimensionPixelSize(R.dimen.width_item_option_popup)
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
        mAdapter.withOnClickListener(this)


        slideInLeftAnimator = SlideInLeftAnimator(AccelerateDecelerateInterpolator())
        slideInRightAnimator = SlideInRightAnimator(AccelerateDecelerateInterpolator())
        marginHorizontal = MARGIN_BORDER
        marginTop = Display.STATUSBAR_HEIGHT + MARGIN_BORDER

        this.also {
            visibility = INVISIBLE
            alpha = 0f
            overScrollMode = 2
            adapter = mAdapter
        }
    }

    fun inBorder(finger: Point): Boolean {
        return inBorderView(this, finger)
    }

    fun onHideView() {
        if (show) {
            show = false
            animate().alpha(0.0f).withEndAction {
                this.visibility = INVISIBLE
                mAdapter.clear()
            }
        }
    }

    fun onShowView(item: Any?, finger: Point) {
        mItem = item

        if (!show) {
            show = true
            this.visibility = VISIBLE
            this.alpha = 1.0f
            val items = getItemOptions((mItem as Item?)!!)
            mAdapter.add(items)
            val position = getPosition(finger)
            this.translationX = position.x.toFloat()
            this.translationY = position.y.toFloat()
        }
    }

    override fun onClick(v: View?, adapter: IAdapter1<OptionAdapter?>, item: OptionAdapter, position: Int): Boolean {
        when (item.identifier.toInt()) {
            INFO -> {
                onInfoItem((mItem as Item?)!!)
            }
            UNINSTALL -> {
                onUninstallItem((mItem as Item?)!!)
            }
            REMOVE -> {
                onRemoveItem((mItem as Item?)!!)
            }
            RESIZE -> {
            }
        }
        mTouchView.dragHelper.setDragState(DragMode.DEFAULT_MODE)
        onHideView()
        return true
    }

    private fun onInfoItem(item: Item) {
        if (item.type === Item.Type.APP) {
            val intent = item.app?.let { Tool.getIntentFromApp(it) }
            val component = intent!!.component ?: return
            val stringBuilder = "package:" + component.packageName
            val intentAction = "android.settings.APPLICATION_DETAILS_SETTINGS"
            val startIntent = Intent(intentAction, Uri.parse(stringBuilder))
            item.context.startActivity(startIntent)
        } else {
            Display.toast(this.context, R.string.toast_app_info_error)
        }
    }

    private fun onUninstallItem(item: Item) {
        if (item.type === Item.Type.APP) {
            val intent = item.app?.let { Tool.getIntentFromApp(it) }
            val component = intent!!.component ?: return
            val stringBuilder = "package:" + component.packageName
            val intentAction = "android.intent.action.DELETE"
            val startIntent = Intent(intentAction, Uri.parse(stringBuilder))
            item.context.startActivity(startIntent)
        } else {
            Display.toast(this.context, R.string.toast_app_uninstalled_error)
        }
    }

    private fun onRemoveItem(item: Item) {
        // folder
        if (item.location == Item.Location.FOLDER) {
            Display.toast(this.context, R.string.toast_remove_from_group_first)
            return
        }

        // items
        val page = mTouchView.dragHelper.currentPage as CellContainer
        page.removeItem(item)
    }

    private fun getItemOptions(item: Item): ArrayList<OptionAdapter?> {
        val itemsList = ArrayList<OptionAdapter?>()
        when (item.type) {
            Item.Type.APP, Item.Type.SHORTCUT -> {
                itemsList.add(infoItem)
                itemsList.add(uninstallItem)

                if (item.location != Item.Location.MENU) {
                    itemsList.add(removeItem)
                }
            }
            Item.Type.WIDGET, Item.Type.FOLDER -> {
                itemsList.add(removeItem)
            }
        }
        return itemsList
    }

    private fun getPosition(finger: Point): Point {
        val posBorder = Point(marginHorizontal, marginTop)

        if (finger.y > displayCenter.y || finger.x < displayCenter.x) {
            val positionX = display.width - itemWidth - marginHorizontal
            posBorder[positionX] = posBorder.y
            this.itemAnimator = slideInRightAnimator
        } else {
            this.itemAnimator = slideInLeftAnimator
        }
        return posBorder
    }

    private fun inBorderView(view: View, finger: Point): Boolean {
        val posView = IntArray(2)
        view.getLocationOnScreen(posView)

        return finger.let {
            (it.x >= posView[0] && it.x <= posView[0] + view.width) // X
                    &&
            (it.y >= posView[1] && it.y <= posView[1] + view.height) // Y
        }
    }
}