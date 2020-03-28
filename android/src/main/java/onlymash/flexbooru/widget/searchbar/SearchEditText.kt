package onlymash.flexbooru.widget.searchbar

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatEditText

class SearchEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private var searchEditTextListener: SearchEditTextListener? = null

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

    fun setSearchEditTextListener(listener: SearchEditTextListener) {
        searchEditTextListener = listener
    }

    interface SearchEditTextListener {
        fun onBackPressed()
    }
}