package onlymash.flexbooru.widget

import android.graphics.Rect
import android.text.Spannable
import android.text.method.TransformationMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import onlymash.flexbooru.util.launchUrl

class LinkTransformationMethod : TransformationMethod {

    override fun getTransformation(source: CharSequence?, view: View?): CharSequence? {
        if (view is TextView) {
            val text = view.text
            if (text is Spannable) {
                text.apply {
                    for (span in getSpans(0, length, URLSpan::class.java)) {
                        setSpan(
                            object : ClickableSpan() {
                                override fun onClick(widget: View) {
                                    view.context.launchUrl(span.url)
                                }
                            },
                            getSpanStart(span),
                            getSpanEnd(span),
                            getSpanFlags(span))
                        removeSpan(span)
                    }
                }
            }
            return text
        }
        return source
    }

    override fun onFocusChanged(
        view: View?,
        sourceText: CharSequence?,
        focused: Boolean,
        direction: Int,
        previouslyFocusedRect: Rect?
    ) {

    }
}