package onlymash.flexbooru.data.action

import okhttp3.HttpUrl
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN

data class ActionArtist(
    var scheme: String = "https",
    var host: String,
    var booruType: Int = BOORU_TYPE_DAN,
    var token: String = "",
    var query: String,
    // danbooru: name, updated_at, post_count (Defaults to ID).
    // moebooru: name, date
    var order: String,
    var limit: Int,
    var username: String = ""
) {
    fun getDanArtistsUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("artists.json")
            .addQueryParameter("search[any_name_matches]", query)
            .addQueryParameter("search[order]", order)
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", username)
            .addQueryParameter("api_key", token)
            .addQueryParameter("only", "id,name,urls")
            .addQueryParameter("commit", "Search")
            .build()
    }

    fun getDan1ArtistsUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("artist")
            .addPathSegment("index.json")
            .addQueryParameter("name", query)
            .addQueryParameter("order", order)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", username)
            .addQueryParameter("password_hash", token)
            .addQueryParameter("commit", "Search")
            .build()
    }

    fun getMoeArtistsUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("artist.json")
            .addQueryParameter("name", query)
            .addQueryParameter("order", order)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", username)
            .addQueryParameter("password_hash", token)
            .addQueryParameter("commit", "Search")
            .build()
    }
}