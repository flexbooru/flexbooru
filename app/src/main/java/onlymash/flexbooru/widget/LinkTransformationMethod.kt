/*
 * Copyright (C) 2019. by onlymash <im@fiepi.me>, All rights reserved
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

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