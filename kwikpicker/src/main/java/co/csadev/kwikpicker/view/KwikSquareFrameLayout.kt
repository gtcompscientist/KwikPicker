package co.csadev.kwikpicker.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

import co.csadev.kwikpicker.R

class KwikSquareFrameLayout(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private var mMatchHeightToWidth: Boolean = false
    private var mMatchWidthToHeight: Boolean = false

    init {
        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.KwikPickerSquareView,
                0,
                0
            )

            try {
                mMatchHeightToWidth =
                        a.getBoolean(R.styleable.KwikPickerSquareView_matchHeightToWidth, false)
                mMatchWidthToHeight =
                        a.getBoolean(R.styleable.KwikPickerSquareView_matchWidthToHeight, false)
            } finally {
                a.recycle()
            }
        }
    }

    //Squares the thumbnail
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (mMatchHeightToWidth)
            setMeasuredDimension(widthMeasureSpec, widthMeasureSpec)
        else if (mMatchWidthToHeight)
            setMeasuredDimension(heightMeasureSpec, heightMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (mMatchHeightToWidth)
            super.onSizeChanged(w, w, oldw, oldh)
        else if (mMatchWidthToHeight)
            super.onSizeChanged(h, h, oldw, oldh)
    }
}
