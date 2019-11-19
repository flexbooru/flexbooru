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
import onlymash.flexbooru.entity.common.Booru
import onlymash.flexbooru.entity.tag.SearchTag

object HydrusUrlHelper {
    /**
     * return hydrus posts request url [HttpUrl]
     * */
    fun getPostUrl(search: Search, page: Int): HttpUrl {

        if (search.host.contains(':')){
            var splits = search.host.split(":")
            val host = splits[0]
            val port = Integer.parseInt(splits[1])
            return HttpUrl.Builder()
                .scheme(search.scheme)
                .host(host)
                .port(port)
                .addPathSegment("get_files")
                .addPathSegment("search_files")
                .addQueryParameter("system_inbox", "true")
                .addQueryParameter("system_archive", "true")
                .addQueryParameter("Hydrus-Client-API-Access-Key", search.auth_key)
                .build()
        }else{
            return HttpUrl.Builder()
                .scheme(search.scheme)
                .host(search.host)
                .addPathSegment("get_files")
                .addPathSegment("search_files")
                .addQueryParameter("system_inbox", "true")
                .addQueryParameter("system_archive", "true")
                .addQueryParameter("Hydrus-Client-API-Access-Key", search.auth_key)
                .build()
        }


    }

    fun getAccessKey(search:Search, name:String): HttpUrl {
        return HttpUrl.Builder()
            .scheme(search.scheme)
            .host(search.host)
            .port(45869)
            .addPathSegment("request_new_permissions")
            .addQueryParameter("name", name)
            .addQueryParameter("basic_permissions", "[0,1,2,3,4,5]")
            .build()

    }

    /**
     * return danbooru user search request url [HttpUrl]
     * */
    fun getUserUrl(username: String, booru: Booru): HttpUrl {
        return HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .port(45869)
            .addPathSegment("verify_access_key")
            .addQueryParameter("Hydrus-Client-API-Access-Key", booru.hashSalt)
            .build()
    }


    fun getTagUrl(search: SearchTag, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(search.scheme)
            .host(search.host)
            .addPathSegment("get_files/search_files")
            .addQueryParameter("system_inbox", "true")
            .addQueryParameter("system_archive", "true")
            .addQueryParameter("Hydrus-Client-API-Access-Key", search.auth_key)
            .build()
    }
    fun getPostCommentUrl(action: CommentAction, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(action.scheme)
            .host(action.host)
            .addPathSegment("get_files/search_files")
            .addQueryParameter("system_inbox", "true")
            .addQueryParameter("system_archive", "true")
            .addQueryParameter("Hydrus-Client-API-Access-Key", action.auth_key)
            .build()
    }
    fun getPostsCommentUrl(action: CommentAction, page: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(action.scheme)
            .host(action.host)
            .addPathSegment("get_files/search_files")
            .addQueryParameter("system_inbox", "true")
            .addQueryParameter("system_archive", "true")
            .addQueryParameter("Hydrus-Client-API-Access-Key", action.auth_key)
            .build()
    }

    fun getAddFavUrl(vote: Vote): HttpUrl {
        return HttpUrl.Builder()
            .scheme(vote.scheme)
            .host(vote.host)
            .addPathSegment("get_files/search_files")
            .addQueryParameter("system_inbox", "true")
            .addQueryParameter("system_archive", "true")

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

//
//    /**
//     * return danbooru1.x tags request url [HttpUrl]
//     * */
//    fun getTagUrl(search: SearchTag, page: Int): HttpUrl {
//        return HttpUrl.Builder()
//            .scheme(search.scheme)
//            .host(search.host)
//            .addPathSegment("tag")
//            .addPathSegment("index.json")
//            .addQueryParameter("name", search.name)
//            .addQueryParameter("order", "count")
//            .addQueryParameter("type", search.type)
//            .addQueryParameter("limit", "40")
//            .addQueryParameter("page", page.toString())
//            .addQueryParameter("login", search.username)
//            .addQueryParameter("password_hash", search.auth_key)
//            .addQueryParameter("commit", "Search")
//            .build()
//    }

}