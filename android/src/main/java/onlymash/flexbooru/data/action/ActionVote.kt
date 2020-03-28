package onlymash.flexbooru.data.action

import okhttp3.HttpUrl
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.model.common.User

data class ActionVote(
    var booru: Booru,
    var user: User? = null,
    var postId: Int
) {
    fun getDan1AddFavUrl(): String =
        String.format("%s://%s/favorite/create.json", booru.scheme, booru.host)

    fun getDan1RemoveFavUrl(): String =
        String.format("%s://%s/favorite/destroy.json", booru.scheme, booru.host)

    fun getDanAddFavUrl(): String =
        String.format("%s://%s/favorites.json", booru.scheme, booru.host)

    fun getDanRemoveFavUrl(): HttpUrl {
        return HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("favorites")
            .addPathSegment("${postId}.json")
            .addQueryParameter("login", user?.name)
            .addQueryParameter("api_key", user?.token)
            .build()
    }

    fun getGelAddFavUrl(): HttpUrl {
        return HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("public")
            .addPathSegment("addfav.php")
            .addQueryParameter("id", postId.toString())
            .build()
    }

    fun getGelRemoveFavUrl(): HttpUrl {
        return HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("index.php")
            .addQueryParameter("page", "favorites")
            .addQueryParameter("s", "delete")
            .addQueryParameter("id", postId.toString())
            .build()
    }

    fun getMoeVoteUrl(): String =
        String.format("%s://%s/post/vote.json", booru.scheme, booru.host)

    fun getSankakuAddFavUrl(): String =
        String.format("%s://%s/favorite/create.json", booru.scheme,
            booru.host.replaceFirst("capi-v2.", "chan."))

    fun getSankakuRemoveFavUrl(): String =
        String.format("%s://%s/favorite/destroy.json", booru.scheme,
            booru.host.replaceFirst("capi-v2.", "chan."))
}