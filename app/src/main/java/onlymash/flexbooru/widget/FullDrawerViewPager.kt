package onlymash.flexbooru.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class FullDrawerViewPager : ViewPager {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)

    private var startX = 0f
    private var startY = 0f

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
                startX = ev.x
                startY = ev.y
            }
            MotionEvent.ACTION_MOVE -> {
                val endX = ev.x
                val endY = ev.y
                val dX = endX - startX
                val dY = endY - startY
                if(Math.abs(dX) > Math.abs(dY)) {
                    if (currentItem == 0 && dX > 0) {
                        parent.requestDisallowInterceptTouchEvent(false)
                    } else if (dX < 0) {
                        adapter?.let {
                            if (currentItem == it.count - 1) {
                                parent.requestDisallowInterceptTouchEvent(true)
                            }
                        }
                    } else {
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}