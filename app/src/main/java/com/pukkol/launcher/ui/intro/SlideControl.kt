package com.pukkol.launcher.ui.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.pukkol.launcher.R
import com.pukkol.launcher.Setup
import io.github.dreierf.materialintroscreen.SlideFragment
import kotlinx.android.synthetic.main.fragment_custom_control.view.*

/**
 * @author okido (Niek Tuytel)[2/8/2021]
 * get from the user what he wants [remote or local] control over the device
 * this information gets saved on the shared preferences
 */
class SlideControl : SlideFragment(), View.OnClickListener {
    private lateinit var layoutLocally: CardView
    private lateinit var layoutRemotely: CardView
    private var moveToNext = false
    var isRemotely = false
        private set

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_custom_control, container, false)
        layoutLocally = view.layout_locally!!
        layoutRemotely = view.layout_remotely!!

        layoutLocally.setOnClickListener(this)
        layoutRemotely.setOnClickListener(this)
        return view
    }

    override fun backgroundColor(): Int {
        return R.color.slide_custom_control_background
    }

    override fun buttonsColor(): Int {
        return R.color.slide_custom_control_buttons
    }

    override fun canMoveFurther(): Boolean {
        if (moveToNext) {
            Setup.deviceSettings().remotelyControlled = isRemotely
        }
        return moveToNext
    }

    override fun cantMoveFurtherErrorMessage(): String {
        return if (isRemotely) {
            getString(R.string.intro_not_available)
        } else {
            getString(R.string.intro_customize_control_error)
        }
    }

    override fun onClick(view: View) {
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.slide_custom_control_background)
        val pressedColor = ContextCompat.getColor(requireContext(), R.color.slide_custom_control_buttons_pressed)
        isRemotely = (view.id == layoutRemotely.id)
        if (view.id == layoutRemotely.id) {
            layoutLocally.setCardBackgroundColor(defaultColor)
            layoutRemotely.setCardBackgroundColor(pressedColor)

            // todo(not developed yet)
            moveToNext = false
            // mMoveToNext = true // ok when developed
        } else if (view.id == layoutLocally.id) {
            layoutLocally.setCardBackgroundColor(pressedColor)
            layoutRemotely.setCardBackgroundColor(defaultColor)
            moveToNext = true
        }
    }
}