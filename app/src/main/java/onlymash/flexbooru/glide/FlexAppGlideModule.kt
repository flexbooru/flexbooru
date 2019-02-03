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
import com.bumptech.glide.request.target.Target
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import onlymash.flexbooru.Constants
import onlymash.flexbooru.util.UserAgent
import java.io.InputStream
import java.util.concurrent.TimeUnit

@GlideModule
class FlexAppGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val memoryCacheSizeBytes: Int = 1024 * 1024 * 128
        val diskCacheSizeBytes: Int = 1024 * 1024 * 256
        builder.setMemoryCache(LruResourceCache(memoryCacheSizeBytes.toLong()))
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, diskCacheSizeBytes.toLong()))
        val requestOptions = RequestOptions
            .formatOf(DecodeFormat.PREFER_ARGB_8888)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .override(Target.SIZE_ORIGINAL)

        builder.setDefaultRequestOptions(requestOptions)
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
        val interceptor = Interceptor { chain ->
            val requests =  chain.request().newBuilder()
                .removeHeader(Constants.USER_AGENT_KEY)
                .addHeader(Constants.USER_AGENT_KEY, UserAgent.get())
                .build()
            chain.proceed(requests)
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