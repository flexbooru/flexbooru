package onlymash.flexbooru.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import okhttp3.OkHttpClient
import onlymash.flexbooru.app.Settings
import onlymash.flexbooru.okhttp.ProgressInterceptor
import java.io.InputStream

@GlideModule
class MyGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val requestOptions = RequestOptions
            .formatOf(DecodeFormat.PREFER_ARGB_8888)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
        builder.setDefaultRequestOptions(requestOptions)
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val builder = OkHttpClient.Builder().addInterceptor(ProgressInterceptor())
        if (Settings.isDohEnable) {
            builder.dns(Settings.doh)
        }
        registry.replace(GlideUrl::class.java, InputStream::class.java, MyOkHttpUrlLoaderFactory(builder.build()))
    }

    override fun isManifestParsingEnabled(): Boolean = false
}