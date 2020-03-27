package onlymash.flexbooru.data.action

import okhttp3.HttpUrl

data class ActionPool(
    var scheme: String = "https",
    var host: String,
    var booruUid: String,
    var booruType: Int = -1,
    var query: String = "",
    var limit: Int = 20,
    var username: String = "",
    var token: String = ""
) {

    fun getDanPoolsUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("pools.json")
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("search[name_matches]", query)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", username)
            .addQueryParameter("api_key", token)
            .build()
    }

    fun getDan1PoolsUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("pool")
            .addPathSegment("index.json")
            .addQueryParameter("query", query)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", username)
            .addQueryParameter("password_hash", token)
            .build()
    }

    fun getMoePoolsUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("pool.json")
            .addQueryParameter("query", query)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", username)
            .addQueryParameter("password_hash", token)
            .build()
    }

    fun getSankakuPoolsUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("pools")
            .addQueryParameter("query", query)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("login", username)
            .addQueryParameter("password_hash", token)
            .build()
    }
}