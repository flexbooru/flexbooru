/*
 * Copyright (C) 2020. by onlymash <fiepi.dev@gmail.com>, All rights reserved
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

data class ActionVote(
    var booru: Booru,
    var postId: Int
) {
    fun getDan1AddFavUrl(): String =
        String.format("%s://%s/favorite/create.json", booru.scheme, booru.host)

    fun getDan1RemoveFavUrl(): String =
        String.format("%s://%s/favorite/destroy.json", booru.scheme, booru.host)

    fun getDanAddFavUrl(): HttpUrl {
        return HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("favorites.json")
            .addQueryParameter("login", booru.user?.name)
            .addQueryParameter("api_key", booru.user?.token)
            .build()
    }

    fun getDanRemoveFavUrl(): HttpUrl {
        return HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("favorites")
            .addPathSegment("${postId}.json")
            .addQueryParameter("login", booru.user?.name)
            .addQueryParameter("api_key", booru.user?.token)
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

    fun getSankakuFavUrl(): HttpUrl {
        return HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("posts")
            .addPathSegment(postId.toString())
            .addPathSegment("favorite")
            .addQueryParameter("lang", "en")
            .build()
    }
}