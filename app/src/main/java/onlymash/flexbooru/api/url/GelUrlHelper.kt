package onlymash.flexbooru.api.url

import okhttp3.HttpUrl
import onlymash.flexbooru.entity.Search

object GelUrlHelper {
    fun getPostUrl(search: Search, page: Int): HttpUrl {
        val userId = search.user_id
        val userIdString = if (userId > 0) userId.toString() else ""
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
            .addQueryParameter("user_id", userIdString)
            .addQueryParameter("api_key", search.auth_key)
            .build()
    }
}