package onlymash.flexbooru.api

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

        onlymash.flexbooru.database.CookieManager.createCookie(Cookie(booru_uid = Settings.instance().activeBooruUid, cookie = cookie))

        return request.newBuilder()
            .removeHeader("Cookie")
            .addHeader("Cookie", cookie)
            .build()
    }

}