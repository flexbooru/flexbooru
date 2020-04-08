/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package onlymash.flexbooru.data.action

import okhttp3.HttpUrl
import onlymash.flexbooru.data.model.common.Booru

data class ActionComment(
    var booru: Booru,
    var limit: Int = 10,
    var body: String = "",
    var commentId: Int = -1,
    var postId: Int = -1,
    var query: String = "",
    // moebooru danbooru: do_not_bump_post Set to 1 if you do not want
    // the post to be bumped to the top of the comment listing
    var anonymous: Int = 0
) {
    fun getDan1PostCommentUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("comment")
            .addPathSegment("index.json")
            .addQueryParameter("post_id", postId.toString())
            .addQueryParameter("page", page.toString())
        booru.user?.let {
            builder.addQueryParameter("login", it.name)
            builder.addQueryParameter("password_hash", it.token)
        }
        return builder.build()
    }
    
    fun getDan1PostsCommentIndexUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("comment")
            .addPathSegment("index.json")
            .addQueryParameter("page", page.toString())
        booru.user?.let {
            builder.addQueryParameter("login", it.name)
            builder.addQueryParameter("password_hash", it.token)
        }
        return builder.build()
    }
    
    fun getDan1PostsCommentSearchUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("comment")
            .addPathSegment("search.json")
            .addQueryParameter("query", query)
            .addQueryParameter("page", page.toString())
        booru.user?.let {
            builder.addQueryParameter("login", it.name)
            builder.addQueryParameter("password_hash", it.token)
        }
        return builder.build()
    }
    
    fun getDan1CreateCommentUrl(): String =
        String.format("%s://%s/comment/create.json", booru.scheme, booru.host)
    
    fun getDan1DestroyCommentUrl(): String =
        String.format("%s://%s/comment/destroy.json", booru.scheme, booru.host)

    fun getDanPostCommentUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("comments.json")
            .addQueryParameter("group_by", "comment")
            .addQueryParameter("search[post_id]", postId.toString())
            .addQueryParameter("search[is_deleted]", "false")
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("only", "creator,id,body,post_id,created_at")
        booru.user?.let {
            builder.addQueryParameter("login", it.name)
            builder.addQueryParameter("api_key", it.token)
        }
        return builder.build()
    }

    /**
     * val builder = danbooru get posts comment request url [HttpUrl]
     * */
    fun getDanPostsCommentUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("comments.json")
            .addQueryParameter("group_by", "comment")
            .addQueryParameter("search[creator_name]", query)
            .addQueryParameter("search[is_deleted]", "false")
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("only", "creator,id,body,post_id,created_at")
        booru.user?.let {
            builder.addQueryParameter("login", it.name)
            builder.addQueryParameter("api_key", it.token)
        }
        return builder.build()
    }

    /**
     * val builder = danbooru delete comment request url [HttpUrl]
     * */
    fun getDanDeleteCommentUrl(): String =
        String.format("%s://%s/comments/%d.json", booru.scheme, booru.host, commentId)

    /**
     * val builder = danbooru create comment request url [String]
     * */
    fun getDanCreateCommentUrl(): String =
        String.format("%s://%s/comments.json", booru.scheme, booru.host)

    fun getGelPostCommentUrl(page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
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
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("index.php")
            .addQueryParameter("page", "dapi")
            .addQueryParameter("s", "comment")
            .addQueryParameter("q", "index")
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("pid", page.toString())
            .build()
    }

    fun getMoePostCommentUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("comment.json")
            .addQueryParameter("post_id", postId.toString())
            .addQueryParameter("page", page.toString())
        booru.user?.let {
            builder.addQueryParameter("login", it.name)
            builder.addQueryParameter("password_hash", it.token)
        }
        return builder.build()
    }
    
    fun getMoePostsCommentUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("comment")
            .addPathSegment("search.json")
            .addQueryParameter("query", query)
            .addQueryParameter("page", page.toString())
        booru.user?.let {
            builder.addQueryParameter("login", it.name)
            builder.addQueryParameter("password_hash", it.token)
        }
        return builder.build()
    }

    fun getMoeCreateCommentUrl(): String =
        String.format("%s://%s/comment/create.json", booru.scheme, booru.host)
    
    fun getMoeDestroyCommentUrl(): String =
        String.format("%s://%s/comment/destroy.json", booru.scheme, booru.host)

    fun getSankakuPostCommentUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host.replaceFirst("capi-v2.", "chan."))
            .addPathSegment("comment")
            .addPathSegment("index.json")
            .addQueryParameter("post_id", postId.toString())
            .addQueryParameter("page", page.toString())
        booru.user?.let {
            builder.addQueryParameter("login", it.name)
            builder.addQueryParameter("password_hash", it.token)
        }
        return builder.build()
    }

    fun getSankakuPostsCommentUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("comments")
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", "25")
        booru.user?.let {
            builder.addQueryParameter("login", it.name)
            builder.addQueryParameter("password_hash", it.token)
        }
        return builder.build()
    }

    fun getSankakuPostsCommentIndexUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host.replaceFirst("capi-v2.", "chan."))
            .addPathSegment("comment")
            .addPathSegment("index.json")
            .addQueryParameter("page", page.toString())
        booru.user?.let {
            builder.addQueryParameter("login", it.name)
            builder.addQueryParameter("password_hash", it.token)
        }
        return builder.build()
    }

    fun getSankakuPostsCommentSearchUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host.replaceFirst("capi-v2.", "chan."))
            .addPathSegment("comment")
            .addPathSegment("search.json")
            .addQueryParameter("query", query)
            .addQueryParameter("page", page.toString())
        booru.user?.let {
            builder.addQueryParameter("login", it.name)
            builder.addQueryParameter("password_hash", it.token)
        }
        return builder.build()
    }

    fun getSankakuCreateCommentUrl(): String =
        String.format("%s://%s/comment/create.json", booru.scheme,
            booru.host.replaceFirst("capi-v2.", "chan."))


    fun getSankakuDestroyCommentUrl(): String =
        String.format("%s://%s/comment/destroy.json", booru.scheme,
            booru.host.replaceFirst("capi-v2.", "chan."))
}