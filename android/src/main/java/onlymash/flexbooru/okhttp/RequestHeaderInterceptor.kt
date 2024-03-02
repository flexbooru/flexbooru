package onlymash.flexbooru.okhttp

import okhttp3.Interceptor
import okhttp3.Response
import onlymash.flexbooru.app.Keys
import onlymash.flexbooru.app.Values

class RequestHeaderInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url
        val scheme = url.scheme
        val host = url.host
        var referer = "$scheme://$host/post"
        val ua = if (host.contains("sankaku", ignoreCase = true)) {
            referer = Values.SANKAKU_REFERER
            Values.PC_USER_AGENT
        } else Values.MOBILE_USER_AGENT
        val builder = chain.request().newBuilder().apply {
            removeHeader(Keys.HEADER_USER_AGENT)
            addHeader(Keys.HEADER_USER_AGENT, ua)
            addHeader(Keys.HEADER_REFERER, referer)
        }
        return chain.proceed(builder.build())
    }
}