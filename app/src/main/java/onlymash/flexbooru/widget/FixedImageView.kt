package onlymash.flexbooru.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import onlymash.flexbooru.R

class FixedImageView(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs) {

    private var widthWeight = 3
    private var heightWeight = 4

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.FixedImageView)
        widthWeight = a.getInteger(R.styleable.FixedImageView_widthWeight, 3)
        heightWeight = a.getInteger(R.styleable.FixedImageView_heightWeight, 4)
        a.recycle()
    }

    fun setWidthAndHeightWeight(widthWeight: Int, heightWeight: Int) {
        this.widthWeight = widthWeight
        this.heightWeight = heightWeight
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = this.measuredWidth
        val height = width * heightWeight / widthWeight
        setMeasuredDimension(width + paddingLeft + paddingRight, height + paddingTop + paddingBottom)
    }
}