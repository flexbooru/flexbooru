/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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

package onlymash.flexbooru.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.core.text.parseAsHtml
import androidx.core.view.updatePadding
import kotlinx.android.synthetic.main.activity_copyright.*
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.R
import onlymash.flexbooru.extension.launchUrl
import onlymash.flexbooru.widget.hideNavBar

class CopyrightActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_copyright)
        hideNavBar {
            scroll_view.updatePadding(bottom = it.systemWindowInsetBottom)
        }
        toolbar.title = getString(R.string.title_copyright)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        copyright.apply {
            text = SpannableStringBuilder(resources.openRawResource(R.raw.copyright).bufferedReader().readText()
                .parseAsHtml(HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_LIST)).apply {
                for (span in getSpans(0, length, URLSpan::class.java)) {
                    setSpan(object : ClickableSpan() {
                        override fun onClick(view: View) {
                            if (span.url.startsWith("mailto:")) {
                                startActivity(Intent.createChooser(Intent().apply {
                                    action = Intent.ACTION_SENDTO
                                    data = span.url.toUri()
                                }, getString(R.string.share_via)))
                            } else launchUrl(span.url)
                        }
                    }, getSpanStart(span), getSpanEnd(span), getSpanFlags(span))
                    removeSpan(span)
                }
            }
//            setBackgroundColor(ContextCompat.getColor(this@CopyrightActivity, R.color.background))
            movementMethod = LinkMovementMethod.getInstance()
        }
    }
}
