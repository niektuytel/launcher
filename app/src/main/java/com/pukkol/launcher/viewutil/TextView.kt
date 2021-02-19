package com.pukkol.launcher.viewutil

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.pukkol.launcher.data.model.Item

class TextView : AppCompatTextView {
    constructor(context: Context?) : super(context!!) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {}
    fun setText(item: Item) {
        if (item.labelVisible) {
            visibility = VISIBLE
            text = item.label
            setTextColor(item.labelColor)
            // setTag(item);
        } else {
            visibility = GONE
        }
    }
}