package onlymash.flexbooru.api

import okhttp3.HttpUrl
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.entity.SearchPopular
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.entity.SearchTag

object ApiUrlHelper {
    fun getDanUrl(search: Search, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(search.scheme)
            .host(search.host)
            .addPathSegment("posts.json")
            .addQueryParameter("limit", search.limit.toString())
            .addQueryParameter("tags", search.keyword)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", search.username)
            .addQueryParameter("api_key", search.auth_key)
            .build()
    }

    fun getMoeUrl(search: Search, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(search.scheme)
            .host(search.host)
            .addPathSegment("post.json")
            .addQueryParameter("limit", search.limit.toString())
            .addQueryParameter("tags", search.keyword)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", search.username)
            .addQueryParameter("password_hash", search.auth_key)
            .build()
    }

    fun getDanPopularUrl(popular: SearchPopular): HttpUrl {
        return HttpUrl.Builder()
            .scheme(popular.scheme)
            .host(popular.host)
            .addPathSegment("explore")
            .addPathSegment("posts")
            .addPathSegment("popular.json")
            .addQueryParameter("date", popular.date)
            .addQueryParameter("scale", popular.scale)
            .addQueryParameter("login", popular.username)
            .addQueryParameter("api_key", popular.auth_key)
            .build()
    }

    fun getMoePopularUrl(popular: SearchPopular): HttpUrl {
        return HttpUrl.Builder()
            .scheme(popular.scheme)
            .host(popular.host)
            .addPathSegment("post")
            .addPathSegment("popular_recent.json")
            .addQueryParameter("period", popular.period)
            .addQueryParameter("login", popular.username)
            .addQueryParameter("password_hash", popular.auth_key)
            .build()
    }

    fun getDanUserUrl(username: String, booru: Booru): HttpUrl {
        return HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("users.json")
            .addQueryParameter("search[name]", username)
            .build()
    }

    fun getMoeUserUrl(username: String, booru: Booru): HttpUrl {
        return HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("user.json")
            .addQueryParameter("name", username)
            .build()
    }

    fun getDanPoolUrl(search: Search, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(search.scheme)
            .host(search.host)
            .addPathSegment("pools.json")
            .addQueryParameter("limit", search.limit.toString())
            .addQueryParameter("search[name_matches]", search.keyword)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", search.username)
            .addQueryParameter("api_key", search.auth_key)
            .build()
    }

    fun getMoePoolUrl(search: Search, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(search.scheme)
            .host(search.host)
            .addPathSegment("pool.json")
            .addQueryParameter("query", search.keyword)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", search.username)
            .addQueryParameter("password_hash", search.auth_key)
            .build()
    }

    fun getDanTagUrl(search: SearchTag, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(search.scheme)
            .host(search.host)
            .addPathSegment("tags.json")
            .addQueryParameter("search[name_matches] ", search.name)
            .addQueryParameter("search[order]", search.order)
            .addQueryParameter("search[category]", search.type)
            .addQueryParameter("search[hide_empty]", "yes")
            .addQueryParameter("limit", search.limit.toString())
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", search.username)
            .addQueryParameter("api_key", search.auth_key)
            .addQueryParameter("commit", "Search")
            .build()
    }

    fun getMoeTagUrl(search: SearchTag, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(search.scheme)
            .host(search.host)
            .addPathSegment("tag.json")
            .addQueryParameter("name", search.name)
            .addQueryParameter("order", search.order)
            .addQueryParameter("type", search.type)
            .addQueryParameter("limit", search.limit.toString())
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", search.username)
            .addQueryParameter("password_hash", search.auth_key)
            .addQueryParameter("commit", "Search")
            .build()
    }
}
