package onlymash.flexbooru.api

import okhttp3.HttpUrl
import onlymash.flexbooru.model.Search

fun getDanbooruUrl(search: Search, page: Int): HttpUrl {
    return HttpUrl.Builder()
        .scheme(search.scheme)
        .host(search.host)
        .addPathSegment("posts.json")
        .addQueryParameter("limit", search.limit.toString())
        .addQueryParameter("tags", search.tags)
        .addQueryParameter("page", page.toString())
        .build()
}

fun getMoebooruUrl(search: Search, page: Int): HttpUrl {
    return HttpUrl.Builder()
        .scheme(search.scheme)
        .host(search.host)
        .addPathSegment("post.json")
        .addQueryParameter("limit", search.limit.toString())
        .addQueryParameter("tags", search.tags)
        .addQueryParameter("page", page.toString())
        .build()
}
