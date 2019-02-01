package onlymash.flexbooru.glide

import com.bumptech.glide.load.model.Headers
import com.bumptech.glide.load.model.LazyHeaders
import onlymash.flexbooru.Constants
import onlymash.flexbooru.util.UserAgent

val glideHeader: Headers
    get() = LazyHeaders.Builder()
        .addHeader(Constants.USER_AGENT_KEY, UserAgent.get())
        .build()