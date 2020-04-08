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

data class ActionPool(
    var booru: Booru,
    var query: String = "",
    var limit: Int = 20
) {

    fun getDanPoolsUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("pools.json")
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("search[name_matches]", query)
            .addQueryParameter("page", page.toString())
        booru.user?.let {
            builder.addQueryParameter("login", it.name)
            builder.addQueryParameter("api_key", it.token)
        }
        return builder.build()
    }

    fun getDan1PoolsUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("pool")
            .addPathSegment("index.json")
            .addQueryParameter("query", query)
            .addQueryParameter("page", page.toString())
        booru.user?.let {
            builder.addQueryParameter("login", it.name)
            builder.addQueryParameter("password_hash", it.token)
        }
        return builder.build()
    }

    fun getMoePoolsUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("pool.json")
            .addQueryParameter("query", query)
            .addQueryParameter("page", page.toString())
        booru.user?.let {
            builder.addQueryParameter("login", it.name)
            builder.addQueryParameter("password_hash", it.token)
        }
        return builder.build()
    }

    fun getSankakuPoolsUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("pools")
            .addQueryParameter("query", query)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", limit.toString())
        booru.user?.let {
            builder.addQueryParameter("login", it.name)
            builder.addQueryParameter("password_hash", it.token)
        }
        return builder.build()
    }
}