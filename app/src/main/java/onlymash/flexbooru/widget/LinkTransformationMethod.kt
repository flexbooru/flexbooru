package onlymash.flexbooru.widget

import android.graphics.Rect
import android.text.Spannable
import android.text.Spanned
import android.text.method.TransformationMethod
import android.text.style.URLSpan
import android.text.util.Linkify
import android.view.View
import android.widget.TextView

class LinkTransformationMethod : TransformationMethod {

    override fun getTransformation(source: CharSequence?, view: View?): CharSequence? {
        if (view is TextView) {
            Linkify.addLinks(view, Linkify.WEB_URLS)
            if (view.text == null || view.text !is Spannable) {
                return source
            }
            val text = view.text as Spannable
            val spans = text.getSpans(0, view.length(), URLSpan::class.java)
            for (i in spans.indices.reversed()) {
                val oldSpan = spans[i]
                val start = text.getSpanStart(oldSpan)
                val end = text.getSpanEnd(oldSpan)
                val url = oldSpan.url
                text.removeSpan(oldSpan)
                text.setSpan(CustomTabsURLSpan(view.context, url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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