package com.pukkol.launcher.ui.home.menu

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.pukkol.launcher.R
import com.pukkol.launcher.Setup
import com.pukkol.launcher.ui.home.FragmentSwitcher
import com.pukkol.launcher.ui.home.HomeTouchView
import com.pukkol.launcher.ui.home.desktop.DesktopFragment
import com.pukkol.launcher.ui.home.menu.MenuPageView.OnMenuPageCallback
import com.pukkol.launcher.util.Display
import kotlinx.android.synthetic.main.view_menu.view.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * @author Okido(Niek Tuytel)[2/10/2021]
 * MenuFragment displays all items init
 * and made possible for the fragment motion
 */
class MenuFragment(private val touchView: HomeTouchView) : Fragment(), OnMenuPageCallback {
    companion object {
        private const val MAX_ALPHA_INTERACTION = 0.20f // 20% of the screen
    }

    var fragmentSwitcher: FragmentSwitcher
        get() = mFragmentSwitcher!!
        set(switcher) {
            mFragmentSwitcher = switcher
            desktopFragment = switcher.desktopFragment
        }

    lateinit var searchLayout: MenuSearchView
        private set

    lateinit var pageLayout: MenuPageView
        private set

    private lateinit var instance: LinearLayout
    private lateinit var instanceParams: FrameLayout.LayoutParams
    private lateinit var separatorView: View

    private var desktopFragment: DesktopFragment? = null
    private var valueAnimator: ValueAnimator? = null
    private var mFragmentSwitcher: FragmentSwitcher? = null
    private var borderY: Float
    private var alpha = 0.000f
    private var isRunning = false

    init {
        borderY = Display.DISPLAY_SIZE!!.height.toFloat()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.view_menu, container, false)
        onCreateInstance(view as LinearLayout)
        onCreatePage()
        onCreateSeparator()
        onCreateSearch()

        instance.setOnTouchListener(pageLayout)
        searchLayout.setOnLayoutListener(pageLayout)
        pageLayout.initView(desktopFragment, this)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Setup.appManager().notifyUpdateListeners()
    }

    override fun onDestroy() {
        onDetachView()
        super.onDestroy()
    }

    override fun onFingerInteract(view: View?, motionEvent: MotionEvent, startY: Float): Boolean {
        return fragmentSwitcher.fingerInteraction.onTouch(motionEvent, startY)
    }

    fun onBackPressed() {
        if (searchLayout.isSearching) {
            onDefaultLayout()
            searchLayout.isSearching(false)
        } else {
            onMotionFragmentDown()
        }
    }

    fun onMotionFragmentUp() {
        onMotionFragment(borderY.toInt(), 0)
    }

    fun onMotionFragmentDown() {
        onMotionFragment(borderY.toInt(), Display.DISPLAY_SIZE!!.height)
    }

    fun onDetachView() {
        if (null != valueAnimator && valueAnimator!!.isRunning) {
            valueAnimator!!.cancel()
        }
        if (null != valueAnimator) {
            valueAnimator = null
        }
    }

    fun setMenuPosition(currentPosition: Float) {
        borderY = currentPosition
        borderY = max(borderY, 0.00f) // minimum
        borderY = min(borderY, Display.DISPLAY_SIZE!!.height.toFloat()) // maximum
        instanceParams.topMargin = borderY.toInt()
        fragmentSwitcher.setSwitchedState(borderY.toInt())
        desktopFragment!!.setAlpha(currentPosition)

        updateAlpha(currentPosition)

        searchLayout.setRadius(currentPosition)
        val id: Int = if (currentPosition < Display.STATUSBAR_HEIGHT) {
            R.color.menu_fragment_background
        } else {
            R.color.transparent
        }

        touchView.setBackgroundColor(
                ContextCompat.getColor(requireContext(), id)
        )
    }

    private fun onDefaultLayout() {
        searchLayout.initView()
        view?.let { Display.Hide.keyboard(requireActivity(), it) }
        pageLayout.setAsDefault()
    }

    private fun onCreateInstance(view: LinearLayout) {
        instance = view.frame_menu_layout
        instanceParams = instance.layoutParams as FrameLayout.LayoutParams
        instanceParams.topMargin = Display.DISPLAY_SIZE!!.height
        instanceParams.width = Display.DISPLAY_SIZE!!.width
        instanceParams.height = Display.DISPLAY_SIZE!!.height
        instance.requestLayout()
    }

    private fun onCreatePage() {
        pageLayout = instance.frame_grid_view
    }

    private fun onCreateSeparator() {
        separatorView = instance.frame_separator
    }

    private fun onCreateSearch() {
        searchLayout = instance.frame_search_layout
        searchLayout.init(
                instance.findViewById<View>(R.id.search_input) as EditText
        )
    }

    private fun onMotionFragment(fY: Int = 0, toY: Int) {
        var fromY = fY

        // cancel animation
        if (valueAnimator != null && isRunning) {
            valueAnimator!!.cancel()
            fromY = Display.DISPLAY_SIZE!!.height
        }

        isRunning = true
        valueAnimator = ValueAnimator.ofInt(fromY, toY).also {
            it.addUpdateListener {
                animation: ValueAnimator ->
                val value = max(animation.animatedValue as Int, 0)
                setMenuPosition(value.toFloat())
            }
            it.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isRunning = false
                    fragmentSwitcher.setSwitchedState(toY)
                    setMenuPosition(toY.toFloat())
                }

                override fun onAnimationStart(animation: Animator) {}
            })

            val displayHeight = Display.DISPLAY_SIZE!!.height
            val distance = abs((toY - fromY) / displayHeight)
            val duration = (300 + 200 * distance).toLong()

            it.duration = duration
            it.start()
        }
    }

    private fun onRequestLayout() {
        searchLayout.requestLayout()
        separatorView.requestLayout()
        pageLayout.requestLayout()
    }

    private fun updateAlpha(posY: Float) {
        val displayHeight = Display.DISPLAY_SIZE!!.height
        val alphaArea = displayHeight * MAX_ALPHA_INTERACTION
        var positionY = posY

        // alpha
        if (positionY < displayHeight - alphaArea) {
            alpha = 1.000f
        } else {
            positionY = displayHeight - positionY
            if (positionY <= alphaArea) {
                alpha = positionY / alphaArea
            }
        }
        instance.alpha = alpha
        onRequestLayout()
    }

}