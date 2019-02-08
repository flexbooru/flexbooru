package onlymash.flexbooru.api

import okhttp3.HttpUrl
import onlymash.flexbooru.model.Booru
import onlymash.flexbooru.model.Popular
import onlymash.flexbooru.model.Search
import onlymash.flexbooru.model.User

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

fun getDanbooruPopularUrl(popular: Popular): HttpUrl {
    return HttpUrl.Builder()
        .scheme(popular.scheme)
        .host(popular.host)
        .addPathSegment("explore")
        .addPathSegment("posts")
        .addPathSegment("popular.json")
        .addQueryParameter("date", popular.date)
        .addQueryParameter("scale", popular.scale)
        .build()
}

fun getMoebooruPopularUrl(popular: Popular): HttpUrl {
    return HttpUrl.Builder()
        .scheme(popular.scheme)
        .host(popular.host)
        .addPathSegment("post")
        .addPathSegment("popular_recent.json")
        .addQueryParameter("period", popular.period)
        .build()
}

fun getDanbooruUserUrl(username: String, booru: Booru): HttpUrl {
    return HttpUrl.Builder()
        .scheme(booru.scheme)
        .host(booru.host)
        .addPathSegment("users.json")
        .addQueryParameter("search[name]", username)
        .build()
}

fun getMoebooruUserUrl(username: String, booru: Booru): HttpUrl {
    return HttpUrl.Builder()
        .scheme(booru.scheme)
        .host(booru.host)
        .addPathSegment("user.json")
        .addQueryParameter("name", username)
        .build()
}
