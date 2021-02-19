package com.pukkol.launcher.ui.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import com.pukkol.launcher.R
import io.github.dreierf.materialintroscreen.SlideFragment
import kotlinx.android.synthetic.main.fragment_custom_agreement.view.*

class SlideAgreement : SlideFragment() {
    private lateinit var checkBox: CheckBox

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_custom_agreement, container, false)
        checkBox = view.checkBox_agreement!!
        return view
    }

    override fun backgroundColor(): Int {
        return R.color.slide_custom_agreement_background
    }

    override fun buttonsColor(): Int {
        return R.color.slide_custom_agreement_buttons
    }

    override fun canMoveFurther(): Boolean {
        return checkBox.isChecked
    }

    override fun cantMoveFurtherErrorMessage(): String {
        return getString(R.string.error_message)
    }
}