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
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.app.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.app.Values.PAGE_TYPE_POPULAR
import onlymash.flexbooru.app.Values.PAGE_TYPE_POSTS
import onlymash.flexbooru.data.model.common.Booru


data class ActionPost(
    var booru: Booru,
    var pageType: Int = PAGE_TYPE_POSTS,
    var limit: Int,
    var query: String = "",
    var isSafeMode: Boolean = true,
    var date: Date,
    // Danbooru: day week month
    var scale: String = "day",
    // Moebooru: 1d 1w 1m 1y
    var period: String = "1d"
) {
    data class Date(
        var yearStart: Int,
        var monthStart: Int,
        var dayStart: Int,
        var yearEnd: Int,
        var monthEnd: Int,
        var dayEnd: Int
    ) {
        fun getDateString(): String {
            val realMonth = monthEnd + 1
            val monthString = if (realMonth < 10) "0$realMonth" else realMonth.toString()
            val dayString = if (dayEnd < 10) "0$dayEnd" else dayEnd.toString()

            return "$yearEnd-$monthString-$dayString"
        }

        fun getDateRangeString(): String {
            val realMonthStart = monthStart + 1
            val monthStringStart = if (realMonthStart < 10) "0$realMonthStart" else realMonthStart.toString()
            val dayStringStart = if (dayStart < 10) "0$dayStart" else dayStart.toString()

            val realMonthEnd = monthEnd + 1
            val monthStringEnd = if (realMonthEnd < 10) "0$realMonthEnd" else realMonthEnd.toString()
            val dayStringEnd = if (dayEnd < 10) "0$dayEnd" else dayEnd.toString()

            return "$yearStart-$monthStringStart-${dayStringStart}T16:00..$yearEnd-$monthStringEnd-${dayStringEnd}T16:00"
        }
    }

    fun isFavoredQuery(): Boolean {
        val user = booru.user ?: return false
        return when (booru.type) {
            BOORU_TYPE_MOE -> query.contains("vote:3:${user.name}")
            BOORU_TYPE_GEL -> query.contains("fav:${user.id}")
            else -> query.contains("fav:${user.name}")
        }
    }

    private fun getPostsDanUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("posts.json")
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("page", page.toString())

        if (isSafeMode && !query.contains(getSafeModeTag(booru.type))) {
            builder.addQueryParameter("tags", "$query ${getSafeModeTag(booru.type)} ${booru.getBlacklistsString()}".trim())
        } else {
            builder.addQueryParameter("tags", "$query ${booru.getBlacklistsString()}".trim())
        }

        booru.user?.let { 
            builder.apply {
                addQueryParameter("login", it.name)
                addQueryParameter("api_key", it.token)
            }
        }
        return builder.build()
    }

    private fun getPostsDan1Url(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("post")
            .addPathSegment("index.json")
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("page", page.toString())

        if (isSafeMode && !query.contains(getSafeModeTag(booru.type))) {
            builder.addQueryParameter("tags", "$query ${getSafeModeTag(booru.type)} ${booru.getBlacklistsString()}".trim())
        } else {
            builder.addQueryParameter("tags", "$query ${booru.getBlacklistsString()}".trim())
        }

        booru.user?.let {
            builder.apply {
                addQueryParameter("login", it.name)
                addQueryParameter("password_hash", it.token)
            }
        }
        return builder.build()
    }

    fun getGelPostsUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("index.php")
            .addQueryParameter("page", "dapi")
            .addQueryParameter("s", "post")
            .addQueryParameter("q", "index")
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("pid", page.toString())

        if (isSafeMode && !query.contains(getSafeModeTag(booru.type))) {
            builder.addQueryParameter("tags", "$query ${getSafeModeTag(booru.type)} ${booru.getBlacklistsString()}".trim())
        } else {
            builder.addQueryParameter("tags", "$query ${booru.getBlacklistsString()}".trim())
        }

        booru.user?.let {
            builder.apply {
                addQueryParameter("user_id", it.id.toString())
                addQueryParameter("api_key", it.token)
            }
        }
        return builder.build()
    }

    fun getPostsMoeUrl(page: Int): HttpUrl {
        val includeVotes = if (booru.host.equals("hypnohub.net", ignoreCase = true)) "0" else "1"
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("post.json")
            .addQueryParameter("include_tags", "1")
            .addQueryParameter("include_votes", includeVotes)
            .addQueryParameter("api_version", "2")
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("page", page.toString())

        if (isSafeMode && !query.contains(getSafeModeTag(booru.type))) {
            builder.addQueryParameter("tags", "$query ${getSafeModeTag(booru.type)} ${booru.getBlacklistsString()}".trim())
        } else {
            builder.addQueryParameter("tags", "$query ${booru.getBlacklistsString()}".trim())
        }

        booru.user?.let {
            builder.apply {
                addQueryParameter("login", it.name)
                addQueryParameter("password_hash", it.token)
            }
        }
        return builder.build()
    }

    private val sankakuBuilder: HttpUrl.Builder
        get() {
            val builder = HttpUrl.Builder()
                .scheme(booru.scheme)
                .host(booru.host)
                .addPathSegments("posts/keyset")
                .addQueryParameter("lang", "en")
                .addQueryParameter("default_threshold", "1")
                .addQueryParameter("hide_posts_in_books", "in-larger-tags")
                .addQueryParameter("limit", limit.toString())
            var tags = if (pageType == PAGE_TYPE_POPULAR) {
                "${booru.getBlacklistsString()} order:popularity date:${date.getDateRangeString()}"
            } else {
                "$query ${booru.getBlacklistsString()}".trim()
            }
            if (isSafeMode && !query.contains(getSafeModeTag(booru.type))) {
                tags = "$tags ${getSafeModeTag(booru.type)}"
            }
            return builder.addQueryParameter("tags", tags)
        }

    fun getSankakuPostsUrl() =
        sankakuBuilder.build()

    fun getSankakuPostsUrlNext(next: String) =
        sankakuBuilder.addQueryParameter("next", next).build()

    private fun getPopularDanUrl(): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("explore")
            .addPathSegment("posts")
            .addPathSegment("popular.json")
            .addQueryParameter("date", date.getDateString())
            .addQueryParameter("scale", scale)
        booru.user?.let {
            builder.apply {
                addQueryParameter("login", it.name)
                addQueryParameter("api_key", it.token)
            }
        }
        return builder.build()
    }

    private fun getPopularDan1Url(): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("post")
            .addPathSegment("popular_by_$scale.json")
            .addQueryParameter("day", date.dayEnd.toString())
            .addQueryParameter("month", (date.monthEnd + 1).toString())
            .addQueryParameter("year", date.yearEnd.toString())

        booru.user?.let {
            builder.apply {
                addQueryParameter("login", it.name)
                addQueryParameter("password_hash", it.token)
            }
        }
        return builder.build()
    }

    fun getPopularMoeUrl(): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("post")
            .addPathSegment("popular_by_$scale.json")
            .addQueryParameter("day", date.dayEnd.toString())
            .addQueryParameter("month", (date.monthEnd + 1).toString())
            .addQueryParameter("year", date.yearEnd.toString())

        booru.user?.let {
            builder.apply {
                addQueryParameter("login", it.name)
                addQueryParameter("password_hash", it.token)
            }
        }
        return builder.build()
    }

    fun getDanPostsUrl(page: Int) =
        if (pageType == PAGE_TYPE_POSTS) getPostsDanUrl(page) else getPopularDanUrl()

    fun getDan1PostsUrl(page: Int) =
        if (pageType == PAGE_TYPE_POSTS) getPostsDan1Url(page) else getPopularDan1Url()


    fun getShimmiePostsUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)

        val path = booru.path
        if (!path.isNullOrBlank()) {
            builder.addPathSegments(path)
        }

        builder.addPathSegment("api")
            .addPathSegment("danbooru")
            .addPathSegment("find_posts")
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("page", page.toString())

        if (query.isNotBlank()) {
            builder.addQueryParameter("tags", query)
        }
        return builder.build()
    }

    companion object {
        fun getSafeModeTag(booruType: Int): String = if (booruType == BOORU_TYPE_DAN) "rating:general" else "rating:safe"
    }
}
