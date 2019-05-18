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

package onlymash.flexbooru.util.okhttp

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.webkit.*
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import onlymash.flexbooru.App
import onlymash.flexbooru.Settings
import onlymash.flexbooru.entity.Cookie
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class CloudflareInterceptor : Interceptor {

    private val serverCheck = arrayOf("cloudflare-nginx", "cloudflare")

    private val handler = Handler(Looper.getMainLooper())

    @Synchronized
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        // Check if Cloudflare anti-bot is on
        if (response.code() == 503 && response.header("Server") in serverCheck) {
            try {
                response.close()
                val solutionRequest = resolveWithWebView(chain.request())
                return chain.proceed(solutionRequest)
            } catch (e: Exception) {
                // Because OkHttp's enqueue only handles IOExceptions, wrap the exception so that
                // we don't crash the entire app
                throw IOException(e)
            }
        }

        return response
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun resolveWithWebView(request: Request): Request {
        // We need to lock this thread until the WebView finds the challenge cookie, because
        // OkHttp doesn't support asynchronous interceptors.
        val latch = CountDownLatch(1)
        var webView: WebView? = null
        var cookie = ""
        val headers = request.headers().toMultimap().mapValues { it.value.getOrNull(0) ?: "" }
        handler.post {
            webView = WebView(App.app).apply {
                settings.javaScriptEnabled = true
                settings.userAgentString = request.header("User-Agent")
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        cookie = CookieManager.getInstance().getCookie(url) ?: ""
                        if(cookie.contains("cf_clearance")) latch.countDown()
                    }
                }
                CookieManager.getInstance().apply {
                    removeAllCookies {}
                    flush()
                }
                loadUrl(request.url().toString(), headers)
            }
        }

        // Wait a reasonable amount of time to retrieve the cookie. The minimum should be
        // around 4 seconds but it can take more due to slow networks or server issues.
        latch.await(15, TimeUnit.SECONDS)

        handler.post {
            webView?.apply {
                stopLoading()
                destroy()
            }
        }

        onlymash.flexbooru.database.CookieManager.createCookie(Cookie(booru_uid = Settings.activeBooruUid, cookie = cookie))

        return request.newBuilder()
            .removeHeader("Cookie")
            .addHeader("Cookie", cookie)
            .build()
    }

}