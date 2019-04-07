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

package onlymash.flexbooru.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.bitmap.ExifInterfaceImageHeaderParser
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import onlymash.flexbooru.Constants
import onlymash.flexbooru.Settings
import onlymash.flexbooru.database.CookieManager
import onlymash.flexbooru.util.UserAgent
import java.io.InputStream
import java.util.concurrent.TimeUnit

@GlideModule
class FlexAppGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val memoryCacheSizeBytes: Int = 1024 * 1024 * 128
        val diskCacheSizeBytes: Int = 1024 * 1024 * 512
        builder.setMemoryCache(LruResourceCache(memoryCacheSizeBytes.toLong()))
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, diskCacheSizeBytes.toLong()))
        val requestOptions = RequestOptions
            .formatOf(DecodeFormat.PREFER_ARGB_8888)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
        builder.setDefaultRequestOptions(requestOptions)
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
        val interceptor = Interceptor {
            val url = it.request().url()
            val scheme = url.scheme()
            var host = url.host()
            if (host.startsWith("cs.")) host = host.replaceFirst("cs.", "beta.")
            val builder = it.request()
                .newBuilder()
                .addHeader(Constants.REFERER_KEY, "$scheme://$host/post")
                .removeHeader(Constants.USER_AGENT_KEY)
                .addHeader(Constants.USER_AGENT_KEY, UserAgent.get())
            CookieManager.getCookieByBooruUid(Settings.instance().activeBooruUid)?.cookie?.let { cookie ->
                builder.addHeader("Cookie", cookie)
            }
            it.proceed(builder.build())
        }
        val client = OkHttpClient.Builder().apply {
            connectTimeout(15, TimeUnit.SECONDS)
            readTimeout(15, TimeUnit.SECONDS)
            writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
        }
            .build()
        val factory = OkHttpUrlLoader.Factory(client)
        registry.replace(GlideUrl::class.java, InputStream::class.java, factory)

        //Hack to fix Glide outputting tons of log spam with ExifInterface errors
        glide.registry.imageHeaderParsers.removeAll { it is ExifInterfaceImageHeaderParser }
    }
}