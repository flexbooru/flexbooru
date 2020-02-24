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
import onlymash.flexbooru.entity.common.Booru
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.entity.Vote
import onlymash.flexbooru.entity.artist.SearchArtist
import onlymash.flexbooru.entity.comment.CommentAction
import onlymash.flexbooru.entity.post.SearchPopular
import onlymash.flexbooru.entity.tag.SearchTag

private const val ONLY_FIELD_POSTS = "id,created_at,score,source,parent_id,preview_file_url,large_file_url,file_url,rating,image_width,image_height,updated_at,created_at,tag_string,tag_string_general,tag_string_character,tag_string_copyright,tag_string_artist,tag_string_meta,is_favorited?,children_ids,pixiv_id,file_size,fav_count,file_ext,uploader"

object DanUrlHelper {
    /**
     * return danbooru posts request url [HttpUrl]
     * */
    fun getPostUrl(search: Search, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(search.scheme)
            .host(search.host)
            .addPathSegment("posts.json")
            .addQueryParameter("limit", search.limit.toString())
            .addQueryParameter("tags", search.keyword)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", search.username)
            .addQueryParameter("api_key", search.auth_key)
            .addQueryParameter("only", ONLY_FIELD_POSTS)
            .build()
    }
    /**
     * return danbooru popular posts request url [HttpUrl]
     * */
    fun getPopularUrl(popular: SearchPopular): HttpUrl {
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
            .addQueryParameter("only", ONLY_FIELD_POSTS)
            .build()
    }

    /**
     * return danbooru user search request url [HttpUrl]
     * */
    fun getUserUrl(username: String, booru: Booru): HttpUrl {
        return HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("users.json")
            .addQueryParameter("search[name]", username)
            .build()
    }

    /**
     * return danbooru pools request url [HttpUrl]
     * */
    fun getPoolUrl(search: Search, page: Int): HttpUrl {
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

    /**
     * return danbooru tags request url [HttpUrl]
     * */
    fun getTagUrl(search: SearchTag, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(search.scheme)
            .host(search.host)
            .addPathSegment("tags.json")
            .addQueryParameter("search[name_matches]", search.name)
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

    /**
     * return danbooru artists request url [HttpUrl]
     * */
    fun getArtistUrl(search: SearchArtist, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(search.scheme)
            .host(search.host)
            .addPathSegment("artists.json")
            .addQueryParameter("search[any_name_matches]", search.name)
            .addQueryParameter("search[order]", search.order)
            .addQueryParameter("limit", search.limit.toString())
            .addQueryParameter("page", page.toString())
            .addQueryParameter("login", search.username)
            .addQueryParameter("api_key", search.auth_key)
            .addQueryParameter("only", "id,name,urls")
            .addQueryParameter("commit", "Search")
            .build()
    }

    /**
     * return danbooru add fav post request url [String]
     * */
    fun getAddFavUrl(vote: Vote): String =
        String.format("%s://%s/favorites.json", vote.scheme, vote.host)

    /**
     * return danbooru remove fav post request url [HttpUrl]
     * */
    fun getRemoveFavUrl(vote: Vote): HttpUrl {
        return HttpUrl.Builder()
            .scheme(vote.scheme)
            .host(vote.host)
            .addPathSegment("favorites")
            .addPathSegment("${vote.post_id}.json")
            .addQueryParameter("login", vote.username)
            .addQueryParameter("api_key", vote.auth_key)
            .build()
    }

    /**
     * return danbooru get post comment request url [HttpUrl]
     * */
    fun getPostCommentUrl(commentAction: CommentAction, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(commentAction.scheme)
            .host(commentAction.host)
            .addPathSegment("comments.json")
            .addQueryParameter("group_by", "comment")
            .addQueryParameter("search[post_id]", commentAction.post_id.toString())
            .addQueryParameter("search[is_deleted]", "false")
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", commentAction.limit.toString())
            .addQueryParameter("login", commentAction.username)
            .addQueryParameter("api_key", commentAction.auth_key)
            .addQueryParameter("only", "creator,id,body,post_id,created_at")
            .build()
    }

    /**
     * return danbooru get posts comment request url [HttpUrl]
     * */
    fun getPostsCommentUrl(commentAction: CommentAction, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(commentAction.scheme)
            .host(commentAction.host)
            .addPathSegment("comments.json")
            .addQueryParameter("group_by", "comment")
            .addQueryParameter("search[creator_name]", commentAction.query)
            .addQueryParameter("search[is_deleted]", "false")
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", commentAction.limit.toString())
            .addQueryParameter("login", commentAction.username)
            .addQueryParameter("api_key", commentAction.auth_key)
            .addQueryParameter("only", "creator,id,body,post_id,created_at")
            .build()
    }

    /**
     * return danbooru delete comment request url [HttpUrl]
     * */
    fun getDeleteCommentUrl(commentAction: CommentAction): String =
        String.format("%s://%s/comments/%d.json", commentAction.scheme, commentAction.host, commentAction.comment_id)

    /**
     * return danbooru create comment request url [String]
     * */
    fun getCreateCommentUrl(commentAction: CommentAction): String =
        String.format("%s://%s/comments.json", commentAction.scheme, commentAction.host)
}