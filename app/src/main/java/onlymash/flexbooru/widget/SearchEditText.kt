package onlymash.flexbooru.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText

class SearchEditText : AppCompatEditText {

    constructor(context: Context) : super(context) 

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) 

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var searchEditTextListener: SearchEditTextListener? = null

    fun setSearchEditTextListener(listener: SearchEditTextListener) {
        searchEditTextListener = listener
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // special case for the back key, we do not even try to send it
            // to the drop down list but instead, consume it immediately
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                keyDispatcherState?.startTracking(event, this)
                return true
            } else if (event.action == KeyEvent.ACTION_UP) {
                keyDispatcherState?.handleUpEvent(event)
                if (event.isTracking && !event.isCanceled) {
                    searchEditTextListener?.let {
                        it.onBackPressed()
                        return true
                    }
                }
            }
        }
        return super.onKeyPreIme(keyCode, event)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            searchEditTextListener?.onClick()
        }
        return super.onTouchEvent(event)
    }

    interface SearchEditTextListener {
        fun onClick()
        fun onBackPressed()
    }
}