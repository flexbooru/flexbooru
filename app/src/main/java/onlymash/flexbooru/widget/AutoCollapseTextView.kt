package onlymash.flexbooru.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isGone

// https://github.com/shadowsocks/shadowsocks-android/blob/77ca235bac46d818b5e985361f7bb31d1d32be3e/core/src/main/java/com/github/shadowsocks/widget/AutoCollapseTextView.kt
class AutoCollapseTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                                     defStyleAttr: Int = 0) :
    AppCompatTextView(context, attrs, defStyleAttr) {
    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        isGone = text.isNullOrEmpty()
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) = try {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
    } catch (e: IndexOutOfBoundsException) {
        e.printStackTrace()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?) = try {
        super.onTouchEvent(event)
    } catch (e: IndexOutOfBoundsException) {
        e.printStackTrace()
        false
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Bug workaround for losing text selection ability, see:
        // https://code.google.com/p/android/issues/detail?id=208169
        isEnabled = false
        isEnabled = true
    }
}