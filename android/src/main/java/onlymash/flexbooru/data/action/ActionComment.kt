package onlymash.flexbooru.data.action

import okhttp3.HttpUrl
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN

data class ActionComment(
    var scheme: String = "https",
    var host: String = "",
    var booruType: Int = BOORU_TYPE_DAN,
    var token: String = "",
    var limit: Int = 10,
    var body: String = "",
    var commentId: Int = -1,
    var postId: Int = -1,
    var query: String = "",
    // moebooru danbooru: do_not_bump_post Set to 1 if you do not want
    // the post to be bumped to the top of the comment listing
    var anonymous: Int = 0,
    var username: String = ""
) {
    fun getDan1PostCommentUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("comment")
            .addPathSegment("index.json")
            .addQueryParameter("post_id", postId.toString())
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", username)
            .addQueryParameter("password_hash", token)
            .build()
    }
    
    fun getDan1PostsCommentIndexUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("comment")
            .addPathSegment("index.json")
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", username)
            .addQueryParameter("password_hash", token)
            .build()
    }
    
    fun getDan1PostsCommentSearchUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("comment")
            .addPathSegment("search.json")
            .addQueryParameter("query", query)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", username)
            .addQueryParameter("password_hash", token)
            .build()
    }
    
    fun getDan1CreateCommentUrl(): String =
        String.format("%s://%s/comment/create.json", scheme, host)
    
    fun getDan1DestroyCommentUrl(): String =
        String.format("%s://%s/comment/destroy.json", scheme, host)

    fun getDanPostCommentUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("comments.json")
            .addQueryParameter("group_by", "comment")
            .addQueryParameter("search[post_id]", postId.toString())
            .addQueryParameter("search[is_deleted]", "false")
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("login", username)
            .addQueryParameter("api_key", token)
            .addQueryParameter("only", "creator,id,body,post_id,created_at")
            .build()
    }

    /**
     * return danbooru get posts comment request url [HttpUrl]
     * */
    fun getDanPostsCommentUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("comments.json")
            .addQueryParameter("group_by", "comment")
            .addQueryParameter("search[creator_name]", query)
            .addQueryParameter("search[is_deleted]", "false")
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("login", username)
            .addQueryParameter("api_key", token)
            .addQueryParameter("only", "creator,id,body,post_id,created_at")
            .build()
    }

    /**
     * return danbooru delete comment request url [HttpUrl]
     * */
    fun getDanDeleteCommentUrl(): String =
        String.format("%s://%s/comments/%d.json", scheme, host, commentId)

    /**
     * return danbooru create comment request url [String]
     * */
    fun getDanCreateCommentUrl(): String =
        String.format("%s://%s/comments.json", scheme, host)

    fun getGelPostCommentUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("index.php")
            .addQueryParameter("page", "dapi")
            .addQueryParameter("s", "comment")
            .addQueryParameter("q", "index")
            .addQueryParameter("post_id", postId.toString())
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("pid", page.toString())
            .build()
    }
    
    fun getGelPostsCommentUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("index.php")
            .addQueryParameter("page", "dapi")
            .addQueryParameter("s", "comment")
            .addQueryParameter("q", "index")
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("pid", page.toString())
            .build()
    }

    fun getMoePostCommentUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("comment.json")
            .addQueryParameter("post_id", postId.toString())
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", username)
            .addQueryParameter("password_hash", token)
            .build()
    }
    
    fun getMoePostsCommentUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("comment")
            .addPathSegment("search.json")
            .addQueryParameter("query", query)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", username)
            .addQueryParameter("password_hash", token)
            .build()
    }

    fun getMoeCreateCommentUrl(): String =
        String.format("%s://%s/comment/create.json", scheme, host)
    
    fun getMoeDestroyCommentUrl(): String =
        String.format("%s://%s/comment/destroy.json", scheme, host)

    fun getSankakuPostCommentUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host.replaceFirst("capi-v2.", "chan."))
            .addPathSegment("comment")
            .addPathSegment("index.json")
            .addQueryParameter("post_id", postId.toString())
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", username)
            .addQueryParameter("password_hash", token)
            .build()
    }

    fun getSankakuPostsCommentUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("comments")
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", "25")
            .addQueryParameter("login", username)
            .addQueryParameter("password_hash", token)
            .build()
    }

    fun getSankakuPostsCommentIndexUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host.replaceFirst("capi-v2.", "chan."))
            .addPathSegment("comment")
            .addPathSegment("index.json")
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", username)
            .addQueryParameter("password_hash", token)
            .build()
    }

    fun getSankakuPostsCommentSearchUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host.replaceFirst("capi-v2.", "chan."))
            .addPathSegment("comment")
            .addPathSegment("search.json")
            .addQueryParameter("query", query)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", username)
            .addQueryParameter("password_hash", token)
            .build()
    }

    fun getSankakuCreateCommentUrl(): String =
        String.format("%s://%s/comment/create.json", scheme,
            host.replaceFirst("capi-v2.", "chan."))


    fun getSankakuDestroyCommentUrl(): String =
        String.format("%s://%s/comment/destroy.json", scheme,
            host.replaceFirst("capi-v2.", "chan."))
}