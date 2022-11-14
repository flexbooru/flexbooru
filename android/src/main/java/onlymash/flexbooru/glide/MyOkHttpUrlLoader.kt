/*
 * Copyright (C) 2020. by onlymash <fiepi.dev@gmail.com>, All rights reserved
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

package onlymash.flexbooru.glide

import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.Headers
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.load.model.ModelLoader
import okhttp3.OkHttpClient
import onlymash.flexbooru.app.Keys
import onlymash.flexbooru.app.Values
import java.io.InputStream
import java.net.URL

class MyOkHttpUrlLoader(client: OkHttpClient) : OkHttpUrlLoader(client) {

    override fun buildLoadData(
        model: GlideUrl,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream>? {
        val url = model.toURL()
        val headers = getHeaders(url)
        return super.buildLoadData(GlideUrl(url, headers), width, height, options)
    }

    private fun getHeaders(url: URL): Headers {
        val scheme = url.protocol
        val host = url.host
        var referer = "$scheme://$host/post"
        val ua = if (host.contains("sankaku", ignoreCase = true)) {
            referer = Values.SANKAKU_REFERER
            Values.PC_USER_AGENT
        } else Values.MOBILE_USER_AGENT
        return LazyHeaders.Builder()
            .addHeader(Keys.HEADER_USER_AGENT, ua)
            .addHeader(Keys.HEADER_REFERER, referer)
            .build()
    }
}