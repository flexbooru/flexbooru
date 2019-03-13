package onlymash.flexbooru.api.url

import okhttp3.HttpUrl
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.entity.comment.CommentAction
import onlymash.flexbooru.entity.tag.SearchTag

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
    fun getTagUrl(search: SearchTag, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(search.scheme)
            .host(search.host)
            .addPathSegment("index.php")
            .addQueryParameter("page", "dapi")
            .addQueryParameter("s", "tag")
            .addQueryParameter("q", "index")
            .addQueryParameter("name_pattern", search.name)
            .addQueryParameter("limit", search.limit.toString())
            .addQueryParameter("pid", page.toString())
            .addQueryParameter("orderby", search.order)
            .build()
    }
    fun getPostCommentUrl(action: CommentAction, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(action.scheme)
            .host(action.host)
            .addPathSegment("index.php")
            .addQueryParameter("page", "dapi")
            .addQueryParameter("s", "comment")
            .addQueryParameter("q", "index")
            .addQueryParameter("post_id", action.post_id.toString())
            .addQueryParameter("limit", action.limit.toString())
            .addQueryParameter("pid", page.toString())
            .build()
    }
    fun getPostsCommentUrl(action: CommentAction, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(action.scheme)
            .host(action.host)
            .addPathSegment("index.php")
            .addQueryParameter("page", "dapi")
            .addQueryParameter("s", "comment")
            .addQueryParameter("q", "index")
            .addQueryParameter("limit", action.limit.toString())
            .addQueryParameter("pid", page.toString())
            .build()
    }
}