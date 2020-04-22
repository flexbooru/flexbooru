package onlymash.flexbooru.glide

import androidx.core.net.toUri
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.*
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader
import onlymash.flexbooru.common.Keys
import onlymash.flexbooru.extension.userAgent
import java.io.InputStream

class HeaderGlideUrlLoader(
    concreteLoader: ModelLoader<GlideUrl, InputStream>,
    modelCache: ModelCache<String, GlideUrl>
) : BaseGlideUrlLoader<String>(concreteLoader, modelCache){

    override fun getUrl(
        model: String,
        width: Int,
        height: Int,
        options: Options?
    ): String = model

    override fun handles(model: String): Boolean = true

    override fun getHeaders(
        model: String,
        width: Int,
        height: Int,
        options: Options?
    ): Headers? {
        val uri = model.toUri()
        val scheme = uri.scheme
        var host = uri.host
        if (host != null && host.startsWith("cs.")) {
            host = host.replaceFirst("cs.", "beta.")
        }
        return LazyHeaders.Builder()
            .addHeader(Keys.HEADER_USER_AGENT, userAgent)
            .addHeader(Keys.HEADER_REFERER, "$scheme://$host/post")
            .build()
    }
}