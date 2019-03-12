package onlymash.flexbooru.api.url

import okhttp3.HttpUrl
import onlymash.flexbooru.entity.Search

object GelUrlHelper {
    fun getPostUrl(search: Search, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(search.scheme)
            .host(search.host)
            .addPathSegment("index.php")
            .addQueryParameter("page", "dapi")
            .addQueryParameter("s", "post")
            .addQueryParameter("q", "index")
            .addQueryParameter("tags", search.keyword)
            .addQueryParameter("limit", search.limit.toString())
            .addQueryParameter("pid", page.toString())
            .addQueryParameter("user_id", search.user_id.toString())
            .addQueryParameter("api_key", search.auth_key)
            .build()
    }
}