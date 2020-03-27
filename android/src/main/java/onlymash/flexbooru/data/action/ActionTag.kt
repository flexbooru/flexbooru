package onlymash.flexbooru.data.action

import okhttp3.HttpUrl
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN

data class ActionTag(
    var scheme: String,
    var host: String,
    var booruType: Int = BOORU_TYPE_DAN,
    var token: String = "",
    var query: String = "",
    //count name date
    var order: String,
    // Moebooru General: 0, artist: 1, copyright: 3, character: 4, Circle: 5, Faults: 6
    // Danbooru General: 0, artist: 1, copyright: 3, character: 4, meta: 5
    // Danbooru1.x General: 0, artist: 1, copyright: 3, character: 4, model: 5, photo_set: 6
    var type: String,
    var limit: Int,
    var username: String = ""
) {
    fun getDanTagsUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("tags.json")
            .addQueryParameter("search[name_matches]", query)
            .addQueryParameter("search[order]", order)
            .addQueryParameter("search[category]", type)
            .addQueryParameter("search[hide_empty]", "yes")
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", username)
            .addQueryParameter("api_key", token)
            .addQueryParameter("commit", "Search")
            .build()
    }

    fun getDan1TagsUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("tag")
            .addPathSegment("index.json")
            .addQueryParameter("name", query)
            .addQueryParameter("order", order)
            .addQueryParameter("type", type)
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", username)
            .addQueryParameter("password_hash", token)
            .addQueryParameter("commit", "Search")
            .build()
    }

    fun getGelTagsUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("index.php")
            .addQueryParameter("page", "dapi")
            .addQueryParameter("s", "tag")
            .addQueryParameter("q", "index")
            .addQueryParameter("name_pattern", query)
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("pid", page.toString())
            .addQueryParameter("orderby", order)
            .build()
    }

    fun getMoeTagsUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("tag.json")
            .addQueryParameter("name", query)
            .addQueryParameter("order", order)
            .addQueryParameter("type", type)
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", username)
            .addQueryParameter("password_hash", token)
            .addQueryParameter("commit", "Search")
            .build()
    }

    fun getSankakuTagsUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("tags")
            .addQueryParameter("name", query)
            .addQueryParameter("order", order)
            .addQueryParameter("type", type)
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", username)
            .addQueryParameter("password_hash", query)
            .addQueryParameter("commit", "Search")
            .build()
    }
}