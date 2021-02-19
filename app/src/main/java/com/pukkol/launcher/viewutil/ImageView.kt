package com.pukkol.launcher.viewutil

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.pukkol.launcher.data.model.Item

class ImageView : AppCompatImageView {
    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {}
    fun setImageBitmap(item: Item) {
        val icon = item.bitmapIcon
        super.setImageBitmap(icon)
    }
}