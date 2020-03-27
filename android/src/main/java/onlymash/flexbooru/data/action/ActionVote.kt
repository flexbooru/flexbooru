package onlymash.flexbooru.data.action

import okhttp3.HttpUrl
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN

data class ActionVote(
    var scheme: String = "https",
    var host: String,
    var booruType: Int = BOORU_TYPE_DAN,
    var username: String = "",
    var token: String = "",
    var booruUid: Long,
    var postId: Int
) {
    fun getDan1AddFavUrl(): String =
        String.format("%s://%s/favorite/create.json", scheme, host)

    fun getDan1RemoveFavUrl(): String =
        String.format("%s://%s/favorite/destroy.json", scheme, host)

    fun getDanAddFavUrl(): String =
        String.format("%s://%s/favorites.json", scheme, host)

    fun getDanRemoveFavUrl(): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("favorites")
            .addPathSegment("${postId}.json")
            .addQueryParameter("login", username)
            .addQueryParameter("api_key", token)
            .build()
    }

    fun getGelAddFavUrl(): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("public")
            .addPathSegment("addfav.php")
            .addQueryParameter("id", postId.toString())
            .build()
    }

    fun getGelRemoveFavUrl(): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("index.php")
            .addQueryParameter("page", "favorites")
            .addQueryParameter("s", "delete")
            .addQueryParameter("id", postId.toString())
            .build()
    }

    fun getMoeVoteUrl(): String =
        String.format("%s://%s/post/vote.json", scheme, host)

    fun getSankakuAddFavUrl(): String =
        String.format("%s://%s/favorite/create.json", scheme,
            host.replaceFirst("capi-v2.", "chan."))

    fun getSankakuRemoveFavUrl(): String =
        String.format("%s://%s/favorite/destroy.json", scheme,
            host.replaceFirst("capi-v2.", "chan."))
}