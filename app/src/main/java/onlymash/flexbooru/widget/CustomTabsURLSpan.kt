package onlymash.flexbooru.widget

import android.content.Context
import android.text.style.URLSpan
import android.view.View
import onlymash.flexbooru.util.launchUrl


class CustomTabsURLSpan(private val context: Context, url: String) : URLSpan(url) {

    override fun onClick(widget: View?) {
        context.launchUrl(url)
    }
}