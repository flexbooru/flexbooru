package onlymash.flexbooru.glide

import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.Headers
import java.net.URL

class FlexGlideUrl : GlideUrl {

    constructor(url: String?) : this(url, glideHeader)

    constructor(url: String?, headers: Headers?): super(url, headers)

    constructor(url: URL?): this(url, glideHeader)

    constructor(url: URL?, headers: Headers?): super(url, headers)
}