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
import onlymash.flexbooru.entity.artist.SearchArtist
import onlymash.flexbooru.entity.comment.CommentAction
import onlymash.flexbooru.entity.post.SearchPopular
import onlymash.flexbooru.entity.tag.SearchTag

object MoeUrlHelper {

    /**
     * return moebooru posts request url [HttpUrl]
     * */
    fun getPostUrl(search: Search, page: Int, tags: String): HttpUrl {
        return HttpUrl.Builder()
            .scheme(search.scheme)
            .host(search.host)
            .addPathSegment("post.json")
            .addQueryParameter("limit", search.limit.toString())
            .addQueryParameter("tags", tags)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", search.username)
            .addQueryParameter("password_hash", search.auth_key)
            .build()
    }

    /**
     * return moebooru popular posts request url [HttpUrl]
     * */
    fun getPopularUrl(popular: SearchPopular): HttpUrl {
        return HttpUrl.Builder()
            .scheme(popular.scheme)
            .host(popular.host)
            .addPathSegment("post")
            .addPathSegment("popular_by_${popular.scale}.json")
            .addQueryParameter("day", popular.day)
            .addQueryParameter("month", popular.month)
            .addQueryParameter("year", popular.year)
            .addQueryParameter("login", popular.username)
            .addQueryParameter("password_hash", popular.auth_key)
            .build()
    }


    /**
     * return moebooru user search request url [HttpUrl]
     * */
    fun getUserUrl(username: String, booru: Booru): HttpUrl {
        return HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("user.json")
            .addQueryParameter("name", username)
            .build()
    }

    /**
     * return moebooru user search by id request url [HttpUrl]
     * */
    fun getUserUrlById(id: Int, booru: Booru): HttpUrl {
        return HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("user.json")
            .addQueryParameter("id", id.toString())
            .build()
    }

    /**
     * return moebooru pools request url [HttpUrl]
     * */
    fun getPoolUrl(search: Search, page: Int): HttpUrl {
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


    /**
     * return moebooru tags request url [HttpUrl]
     * */
    fun getTagUrl(search: SearchTag, page: Int): HttpUrl {
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



    /**
     * return moebooru artists request url [HttpUrl]
     * */
    fun getArtistUrl(search: SearchArtist, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(search.scheme)
            .host(search.host)
            .addPathSegment("artist.json")
            .addQueryParameter("name", search.name)
            .addQueryParameter("order", search.order)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", search.username)
            .addQueryParameter("password_hash", search.auth_key)
            .addQueryParameter("commit", "Search")
            .build()
    }


    /**
     * return moebooru vote post request url [String]
     * */
    fun getVoteUrl(vote: Vote): String =
        String.format("%s://%s/post/vote.json", vote.scheme, vote.host)

    /**
     * return moebooru get post comments url [HttpUrl]
     * */
    fun getPostCommentUrl(commentAction: CommentAction, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(commentAction.scheme)
            .host(commentAction.host)
            .addPathSegment("comment.json")
            .addQueryParameter("post_id", commentAction.post_id.toString())
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", commentAction.username)
            .addQueryParameter("password_hash", commentAction.auth_key)
            .build()
    }

    /**
     * return moebooru get posts comments request url [HttpUrl]
     * */
    fun getPostsCommentUrl(commentAction: CommentAction, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(commentAction.scheme)
            .host(commentAction.host)
            .addPathSegment("comment")
            .addPathSegment("search.json")
            .addQueryParameter("query", commentAction.query)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", commentAction.username)
            .addQueryParameter("password_hash", commentAction.auth_key)
            .build()
    }

    /**
     * return moebooru create comment request url [String]
     * */
    fun getCreateCommentUrl(commentAction: CommentAction): String =
        String.format("%s://%s/comment/create.json", commentAction.scheme, commentAction.host)

    /**
     * return moebooru delete comment request url [String]
     * */
    fun getDestroyCommentUrl(commentAction: CommentAction): String =
        String.format("%s://%s/comment/destroy.json", commentAction.scheme, commentAction.host)
}
