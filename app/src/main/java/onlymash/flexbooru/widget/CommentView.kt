/*
 * Copyright (C) 2019 by onlymash <im@fiepi.me>, All rights reserved
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

import android.content.Context
import android.text.util.Linkify
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import onlymash.flexbooru.R


class CommentView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private data class Comment(
        val text: String,
        val type: Int
    )

    private fun getComments(body: String): MutableList<Comment> {
        val comments: MutableList<Comment> = mutableListOf()
        body.split("[/quote]").forEach {
            if (!it.contains("[quote]") && !it.isEmpty()) {
                comments.add(Comment(text = it, type = 0))
            } else {
                it.split("[quote]").forEachIndexed { index, s ->
                    if (index == 0 && !s.isEmpty()) {
                        comments.add(Comment(text = s, type = 0))
                    } else if(index == 1 && !s.isEmpty()) {
                        comments.add(Comment(text = s, type = 1))
                    }
                }
            }
        }
        return comments
    }

    internal fun setComment(body: String) {
        getComments(body).forEach {
            if (it.type == 0) {
                addComment(it.text)
            } else {
                addQuote(it.text)
            }
        }
    }

    private fun addComment(comment: String) {
        val margin = context.resources.getDimensionPixelSize(R.dimen.spacing_medium)
        val textView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(margin, margin, margin, margin)
            }
            setTextIsSelectable(true)
            autoLinkMask = Linkify.WEB_URLS
            linksClickable = true
            transformationMethod = LinkTransformationMethod()
            setTextAppearance(android.R.style.TextAppearance_Material_Body1)
            text = comment
        }
        addView(textView)
    }

    private fun addQuote(quote: String) {
        val margin = context.resources.getDimensionPixelSize(R.dimen.spacing_medium)
        val layout = FrameLayout(context).apply {
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setBackgroundColor(ContextCompat.getColor(context, R.color.background_quote))
            }
        }
        val textView = TextView(context).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(margin * 2, margin, margin * 2, margin)
            }
            setTextIsSelectable(true)
            autoLinkMask = Linkify.WEB_URLS
            linksClickable = true
            transformationMethod = LinkTransformationMethod()
            setTextAppearance(android.R.style.TextAppearance_Material_Body1)
            text = quote
        }
        layout.addView(textView)
        addView(layout)
    }
}