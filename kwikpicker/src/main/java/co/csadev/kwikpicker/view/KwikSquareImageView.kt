package co.csadev.kwikpicker.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet

import co.csadev.kwikpicker.R

class KwikSquareImageView(context: Context, attrs: AttributeSet? = null) : AppCompatImageView(context, attrs) {

    private var fit_mode: String? = null
    var foregroundDrawable: Drawable? = null
        set(value) {
            if (field === value) {
                return
            }
            field?.let {
                it.callback = null
                unscheduleDrawable(it)
            }

            field = value
            field?.callback = this
            if (field?.isStateful == true)
                field?.state = drawableState
            requestLayout()
            invalidate()
        }

    init {
        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.KwikPickerImageView,
                0, 0
            )
            foregroundDrawable = a.getDrawable(R.styleable.KwikPickerImageView_foreground)
            try {
                fit_mode = a.getString(R.styleable.KwikPickerImageView_fit_mode)
            } finally {
                a.recycle()
            }
        }
    }

    //Squares the thumbnail
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if ("height" == fit_mode) {
            setMeasuredDimension(heightMeasureSpec, heightMeasureSpec)
        } else {
            setMeasuredDimension(widthMeasureSpec, widthMeasureSpec)
        }
        if (foregroundDrawable != null) {
            foregroundDrawable!!.setBounds(0, 0, measuredWidth, measuredHeight)
            invalidate()
        }
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who === foregroundDrawable
    }

    override fun jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState()
        foregroundDrawable?.jumpToCurrentState()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        if (foregroundDrawable?.isStateful == true)
            foregroundDrawable?.state = drawableState
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        foregroundDrawable?.let {
            it.setBounds(0, 0, w, h)
            invalidate()
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        foregroundDrawable?.draw(canvas)
    }
}
