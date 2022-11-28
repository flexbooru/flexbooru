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

package onlymash.flexbooru.okhttp

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException

import android.os.StatFs
import androidx.annotation.VisibleForTesting
import okhttp3.*
import onlymash.flexbooru.app.App
import onlymash.flexbooru.app.Keys.HEADER_REFERER
import onlymash.flexbooru.app.Keys.HEADER_USER_AGENT
import onlymash.flexbooru.app.Settings
import onlymash.flexbooru.app.Values
import kotlin.math.max
import kotlin.math.min

/** A [Downloader] which uses OkHttp to download images.  */
class OkHttp3Downloader : Downloader {

    @VisibleForTesting
    internal val client: Call.Factory
    private val cache: Cache?
    private var sharedClient = true

    /**
     * Create new downloader that uses OkHttp. This will install an image cache into your application
     * cache directory.
     */
    constructor(context: Context) : this(createDefaultCacheDir(context))

    /**
     * Create new downloader that uses OkHttp. This will install an image cache into your application
     * cache directory.
     *
     * @param maxSize The size limit for the cache.
     */
    constructor(context: Context, maxSize: Long) : this(createDefaultCacheDir(context), maxSize)

    /**
     * Create new downloader that uses OkHttp. This will install an image cache into the specified
     * directory.
     *
     * @param cacheDir The directory in which the cache should be stored
     * @param maxSize The size limit for the cache.
     */
    @JvmOverloads
    constructor(
        cacheDir: File,
        maxSize: Long = calculateDiskCacheSize(cacheDir)) : this(createOkHttpClient(cacheDir, maxSize)) {
        sharedClient = false
    }

    /**
     * Create a new downloader that uses the specified OkHttp instance. A response cache will not be
     * automatically configured.
     */
    constructor(client: OkHttpClient) {
        this.client = client
        this.cache = client.cache
    }

    /** Create a new downloader that uses the specified [Call.Factory] instance.  */
    constructor(client: Call.Factory) {
        this.client = client
        this.cache = null
    }

    @Throws(IOException::class)
    override fun load(request: Request): Response {
        return client.newCall(request).execute()
    }

    @Throws(IOException::class)
    override fun load(uri: Uri): Response {
        val request = Request.Builder()
            .url(uri.toString())
            .build()
        return load(request)
    }

    @Throws(IOException::class)
    override fun load(url: String): Response {
        val request = Request.Builder()
            .url(url)
            .build()
        return load(request)
    }

    override fun shutdown() {
        if (!sharedClient && cache != null) {
            try {
                cache.close()
            } catch (ignored: IOException) {
            }
        }
    }

    companion object {
        private const val MIN_DISK_CACHE_SIZE = 5 * 1024 * 1024 // 5MB
        private const val MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024 // 50MB

        /**
         * Create new downloader that uses OkHttp. This will install an image cache into the specified
         * directory.
         */
        private fun createDefaultCacheDir(context: Context): File {
            val cache = File(context.applicationContext.cacheDir, "downloader")
            if (!cache.exists()) {
                cache.mkdirs()
            }
            return cache
        }

        private fun calculateDiskCacheSize(dir: File): Long {
            var size = MIN_DISK_CACHE_SIZE.toLong()
            try {
                val statFs = StatFs(dir.absolutePath)
                val blockCount = statFs.blockCountLong
                val blockSize = statFs.blockSizeLong
                val available = blockCount * blockSize
                // Target 2% of the total space.
                size = available / 50
            } catch (ignored: IllegalArgumentException) {
            }
            // Bound inside min/max size for disk cache.
            return max(min(size, MAX_DISK_CACHE_SIZE.toLong()), MIN_DISK_CACHE_SIZE.toLong())
        }
        private fun createOkHttpClient(cacheDir: File, maxSize: Long): OkHttpClient {
            val interceptor = Interceptor {
                val url = it.request().url
                val scheme = url.scheme
                val host = url.host
                it.proceed(it.request()
                    .newBuilder()
                    .header(HEADER_USER_AGENT, Values.MOBILE_USER_AGENT)
                    .header(HEADER_REFERER, "$scheme://$host/post")
                    .build())
            }
            val builder = OkHttpClient.Builder()
                .cache(Cache(cacheDir, maxSize))
                .cookieJar(AndroidCookieJar)
                .addInterceptor(interceptor)
                .addInterceptor(CloudflareInterceptor(App.app))
                .addInterceptor(ProgressInterceptor())
            if (Settings.isDohEnable) {
                builder.dns(Settings.doh)
            }
            return builder.build()
        }
    }
}
