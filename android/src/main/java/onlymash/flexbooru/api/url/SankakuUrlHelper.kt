/*
 * Copyright (C) 2019. by onlymash <im@fiepi.me>, All rights reserved
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

package onlymash.flexbooru.api.url

import okhttp3.HttpUrl
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.entity.Vote
import onlymash.flexbooru.entity.comment.CommentAction
import onlymash.flexbooru.entity.post.SearchPopular
import onlymash.flexbooru.entity.tag.SearchTag

object SankakuUrlHelper {

    fun getPostUrl(search: Search, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(search.scheme)
            .host(search.host)
            .addPathSegment("posts")
            .addQueryParameter("limit", search.limit.toString())
            .addQueryParameter("tags", search.keyword)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", search.username)
            .addQueryParameter("password_hash", search.auth_key)
            .build()
    }


    fun getPopularUrl(popular: SearchPopular, page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(popular.scheme)
            .host(popular.host)
            .addPathSegment("posts")
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", popular.limit.toString())
            .addQueryParameter("login", popular.username)
            .addQueryParameter("password_hash", popular.auth_key)
        if (popular.date.isNotEmpty()) {
            builder.addQueryParameter("tags", "order:popular date:${popular.date}")
        } else {
            builder.addQueryParameter("tags", "order:popular")
        }
        return builder.build()
    }

    fun getPoolUrl(search: Search, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(search.scheme)
            .host(search.host)
            .addPathSegment("pools")
            .addQueryParameter("query", search.keyword)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", search.limit.toString())
            .addQueryParameter("login", search.username)
            .addQueryParameter("password_hash", search.auth_key)
            .build()
    }

    fun getTagUrl(search: SearchTag, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(search.scheme)
            .host(search.host)
            .addPathSegment("tags")
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

    fun getUserUrl(username: String, booru: Booru): HttpUrl {
        return HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("users")
            .addQueryParameter("name", username)
            .build()
    }

    fun getAddFavUrl(vote: Vote): String =
        String.format("%s://%s/favorite/create.json", vote.scheme,
            vote.host.replaceFirst("capi-v2.", "chan."))

    fun getRemoveFavUrl(vote: Vote): String =
        String.format("%s://%s/favorite/destroy.json", vote.scheme,
            vote.host.replaceFirst("capi-v2.", "chan."))

    fun getPostCommentUrl(commentAction: CommentAction, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(commentAction.scheme)
            .host(commentAction.host.replaceFirst("capi-v2.", "chan."))
            .addPathSegment("comment")
            .addPathSegment("index.json")
            .addQueryParameter("post_id", commentAction.post_id.toString())
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", commentAction.username)
            .addQueryParameter("password_hash", commentAction.auth_key)
            .build()
    }

    fun getPostsCommentUrl(commentAction: CommentAction, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(commentAction.scheme)
            .host(commentAction.host)
            .addPathSegment("comments")
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", "25")
            .addQueryParameter("login", commentAction.username)
            .addQueryParameter("password_hash", commentAction.auth_key)
            .build()
    }

    fun getPostsCommentIndexUrl(commentAction: CommentAction, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(commentAction.scheme)
            .host(commentAction.host.replaceFirst("capi-v2.", "chan."))
            .addPathSegment("comment")
            .addPathSegment("index.json")
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", commentAction.username)
            .addQueryParameter("password_hash", commentAction.auth_key)
            .build()
    }

    fun getPostsCommentSearchUrl(commentAction: CommentAction, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(commentAction.scheme)
            .host(commentAction.host.replaceFirst("capi-v2.", "chan."))
            .addPathSegment("comment")
            .addPathSegment("search.json")
            .addQueryParameter("query", commentAction.query)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", commentAction.username)
            .addQueryParameter("password_hash", commentAction.auth_key)
            .build()
    }

    fun getCreateCommentUrl(commentAction: CommentAction): String =
        String.format("%s://%s/comment/create.json", commentAction.scheme,
            commentAction.host.replaceFirst("capi-v2.", "chan."))


    fun getDestroyCommentUrl(commentAction: CommentAction): String =
        String.format("%s://%s/comment/destroy.json", commentAction.scheme,
            commentAction.host.replaceFirst("capi-v2.", "chan."))
}