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

data class ActionArtist(
    var booru: Booru,
    var query: String,
    // danbooru: name, updated_at, post_count (Defaults to ID).
    // moebooru: name, date
    var order: String,
    var limit: Int
) {
    fun getDanArtistsUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("artists.json")
            .addQueryParameter("search[any_name_matches]", query)
            .addQueryParameter("search[order]", order)
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("page", page.toString())
            .addQueryParameter("only", "id,name,urls")
            .addQueryParameter("commit", "Search")
        booru.user?.let {
            builder.addQueryParameter("login", it.name)
            builder.addQueryParameter("api_key", it.token)
        }
        return builder.build()
    }

    fun getDan1ArtistsUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("artist")
            .addPathSegment("index.json")
            .addQueryParameter("name", query)
            .addQueryParameter("order", order)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("commit", "Search")
        booru.user?.let {
            builder.addQueryParameter("login", it.name)
            builder.addQueryParameter("api_key", it.token)
        }
        return builder.build()
    }

    fun getMoeArtistsUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("artist.json")
            .addQueryParameter("name", query)
            .addQueryParameter("order", order)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("commit", "Search")
        booru.user?.let {
            builder.addQueryParameter("login", it.name)
            builder.addQueryParameter("password_hash", it.token)
        }
        return builder.build()
    }
}