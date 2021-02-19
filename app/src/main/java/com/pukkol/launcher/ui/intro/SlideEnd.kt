package com.pukkol.launcher.ui.intro

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pukkol.launcher.MainActivity
import com.pukkol.launcher.R
import com.pukkol.launcher.Setup
import com.pukkol.launcher.util.Display
import io.github.dreierf.materialintroscreen.SlideFragment
import kotlinx.android.synthetic.main.fragment_custom_finished.view.*

class SlideEnd : SlideFragment(), View.OnClickListener {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_custom_finished, container, false)

        val button = view.button_start_pukkol
        button.setOnClickListener(this)

        setCellWidth()
        return view
    }

    override fun onClick(v: View?) {
        startMainActivity()
    }

    override fun backgroundColor(): Int {
        return R.color.slide_custom_control_background
    }

    override fun buttonsColor(): Int {
        return R.color.slide_custom_control_background
    }

    override fun canMoveFurther(): Boolean {
        return false
    }

    override fun cantMoveFurtherErrorMessage(): String {
        return "error"
    }

    private fun startMainActivity() {
        // Setup.deviceSettings().setIntroLaunch(true); // TODO(testing)
        Setup.deviceSettings().introLaunch = false
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
    }

    private fun setCellWidth() {
        val displayWidth = Display.DISPLAY_SIZE!!.width
        val displayCellWidth = Setup.deviceSettings().cellHorizontalAmount
        Setup.deviceSettings().cellWidth = (displayWidth / displayCellWidth)
    }
}