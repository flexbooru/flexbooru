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
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.entity.Vote
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

    fun getAddFavUrl(vote: Vote): HttpUrl {
        return HttpUrl.Builder()
            .scheme(vote.scheme)
            .host(vote.host)
            .addPathSegment("public")
            .addPathSegment("addfav.php")
            .addQueryParameter("id", vote.post_id.toString())
            .build()
    }

    fun getRemoveFavUrl(vote: Vote): HttpUrl {
        return HttpUrl.Builder()
            .scheme(vote.scheme)
            .host(vote.host)
            .addPathSegment("index.php")
            .addQueryParameter("page", "favorites")
            .addQueryParameter("s", "delete")
            .addQueryParameter("id", vote.post_id.toString())
            .build()
    }
}