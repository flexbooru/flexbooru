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

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.webkit.*
import android.widget.Toast
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import onlymash.flexbooru.R
import onlymash.flexbooru.app.App
import onlymash.flexbooru.app.Keys.HEADER_COOKIE
import onlymash.flexbooru.app.Keys.HEADER_USER_AGENT
import onlymash.flexbooru.app.Values.MOBILE_USER_AGENT
import onlymash.flexbooru.data.database.MyCookieManager
import onlymash.flexbooru.data.model.common.Cookie
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object CloudflareInterceptor : Interceptor {

    private val handler = Handler(Looper.getMainLooper())

    @Synchronized
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val cookie = MyCookieManager.getCookieByHost(request.url.host)
        if (cookie != null) {
            request = request.newBuilder()
                .header(HEADER_COOKIE, cookie.cookie)
                .build()
        }
        val response = chain.proceed(request)
        // Check if Cloudflare anti-bot is on
        if (response.header("CF-Chl-Bypass") == "1") {
            try {
                response.close()
                val solutionRequest = resolveWithWebView(request)
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
        val headers = request.headers.toMultimap().mapValues { it.value.getOrNull(0) ?: "" }
        handler.post {
            webView = WebView(App.app).apply {
                settings.javaScriptEnabled = true
                settings.userAgentString = request.header(HEADER_USER_AGENT) ?: MOBILE_USER_AGENT
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        cookie = CookieManager.getInstance().getCookie(url) ?: ""
                        if (cookie.contains("cf_clearance")) latch.countDown()
                    }
                }
                CookieManager.getInstance().apply {
                    removeAllCookies {}
                    flush()
                }
                loadUrl(request.url.toString(), headers)
            }
        }

        // Wait a reasonable amount of time to retrieve the cookie. The minimum should be
        // around 4 seconds but it can take more due to slow networks or server issues.
        latch.await(20, TimeUnit.SECONDS)

        // Find cookie value and domain
        var testUrl = request.url
        var host = testUrl.host
        while (true) {
            testUrl = testUrl.newBuilder()
                .host(host.substringAfter('.', ""))
                .build()
            val testCookie = CookieManager.getInstance().getCookie(testUrl.toString()) ?: ""
            if (testCookie.contains("cf_clearance")) {
                host = testUrl.host
                cookie = testCookie
            } else break
        }
        cookie = cookie.split("; ").find {
            it.startsWith("cf_clearance")
        } ?: ""

        handler.post {
            webView?.apply {
                stopLoading()
                destroy()
            }
            if (cookie.isEmpty()) {
                Toast.makeText(App.app, R.string.msg_cloudflare_challenges, Toast.LENGTH_SHORT)
                    .show()
            }
        }

        if (cookie.isNotEmpty()) {
            MyCookieManager.createCookie(Cookie(
                host = host,
                cookie = cookie
            ))
        }

        return request.newBuilder()
            .header(HEADER_COOKIE, cookie)
            .build()
    }

}