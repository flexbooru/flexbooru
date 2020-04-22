package onlymash.flexbooru.glide

import com.bumptech.glide.load.model.*
import java.io.InputStream

class HeaderModelLoaderFactory : ModelLoaderFactory<String, InputStream> {

    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<String, InputStream> {
        return HeaderGlideUrlLoader(
            concreteLoader = multiFactory.build(GlideUrl::class.java, InputStream::class.java),
            modelCache = ModelCache<String, GlideUrl>(500)
        )
    }

    override fun teardown() {

    }
}