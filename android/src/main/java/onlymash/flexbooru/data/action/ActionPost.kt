package onlymash.flexbooru.data.action

import okhttp3.HttpUrl
import onlymash.flexbooru.common.Values.ONLY_FIELD_POSTS_DAN
import onlymash.flexbooru.common.Values.PAGE_TYPE_POSTS
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.model.common.User

private const val SAFE_MODE_TAG = "rating:safe"

data class ActionPost(
    var booru: Booru,
    var user: User? = null,
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
        var year: Int,
        var month: Int,
        var day: Int,
        var yearEnd: Int,
        var monthEnd: Int,
        var dayEnd: Int
    ) {
        fun getDateString(): String {
            val realMonth = month + 1
            val monthString = if (realMonth < 10) "0$realMonth" else realMonth.toString()
            val dayString = if (day < 10) "0$day" else day.toString()

            return "$year-$monthString-$dayString"
        }

        fun getDateRangeString(): String {
            val realMonth = month + 1
            val monthString = if (realMonth < 10) "0$realMonth" else realMonth.toString()
            val dayString = if (day < 10) "0$day" else day.toString()

            val realMonthEnd = monthEnd + 1
            val monthStringEnd = if (realMonthEnd < 10) "0$realMonthEnd" else realMonthEnd.toString()
            val dayStringEnd = if (dayEnd < 10) "0$dayEnd" else dayEnd.toString()

            return "$dayString.$monthString.$year..$dayStringEnd.$monthStringEnd.$yearEnd"
        }
    }

    private fun getPostsDanUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.scheme)
            .addPathSegment("posts.json")
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("page", page.toString())
            .addQueryParameter("only", ONLY_FIELD_POSTS_DAN)

        if (isSafeMode && !query.contains(SAFE_MODE_TAG)) {
            builder.addQueryParameter("tags", "$query $SAFE_MODE_TAG ${booru.getBlacklistsString()}".trim())
        } else {
            builder.addQueryParameter("tags", "$query ${booru.getBlacklistsString()}".trim())
        }

        user?.let { 
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

        if (isSafeMode && !query.contains(SAFE_MODE_TAG)) {
            builder.addQueryParameter("tags", "$query $SAFE_MODE_TAG ${booru.getBlacklistsString()}".trim())
        } else {
            builder.addQueryParameter("tags", "$query ${booru.getBlacklistsString()}".trim())
        }

        user?.let {
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

        if (isSafeMode && !query.contains(SAFE_MODE_TAG)) {
            builder.addQueryParameter("tags", "$query $SAFE_MODE_TAG ${booru.getBlacklistsString()}".trim())
        } else {
            builder.addQueryParameter("tags", "$query ${booru.getBlacklistsString()}".trim())
        }

        user?.let {
            builder.apply {
                addQueryParameter("user_id", it.id.toString())
                addQueryParameter("api_key", it.token)
            }
        }
        return builder.build()
    }

    private fun getPostsMoeUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("post.json")
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("page", page.toString())

        if (isSafeMode && !query.contains(SAFE_MODE_TAG)) {
            builder.addQueryParameter("tags", "$query $SAFE_MODE_TAG ${booru.getBlacklistsString()}".trim())
        } else {
            builder.addQueryParameter("tags", "$query ${booru.getBlacklistsString()}".trim())
        }

        user?.let {
            builder.apply {
                addQueryParameter("login", it.name)
                addQueryParameter("password_hash", it.token)
            }
        }
        return builder.build()
    }

    private fun getPostsSankakuUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("posts")
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("page", page.toString())

        if (isSafeMode && !query.contains(SAFE_MODE_TAG)) {
            builder.addQueryParameter("tags", "$query $SAFE_MODE_TAG ${booru.getBlacklistsString()}".trim())
        } else {
            builder.addQueryParameter("tags", "$query ${booru.getBlacklistsString()}".trim())
        }

        user?.let {
            builder.apply {
                addQueryParameter("login", it.name)
                addQueryParameter("password_hash", it.token)
            }
        }
        return builder.build()
    }

    private fun getPopularDanUrl(): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("explore")
            .addPathSegment("posts")
            .addPathSegment("popular.json")
            .addQueryParameter("date", date.getDateString())
            .addQueryParameter("scale", scale)
            .addQueryParameter("only", ONLY_FIELD_POSTS_DAN)
        user?.let {
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
            .addQueryParameter("day", date.day.toString())
            .addQueryParameter("month", (date.month + 1).toString())
            .addQueryParameter("year", date.year.toString())

        user?.let {
            builder.apply {
                addQueryParameter("login", it.name)
                addQueryParameter("password_hash", it.token)
            }
        }
        return builder.build()
    }

    private fun getPopularMoeUrl(): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("post")
            .addPathSegment("popular_by_$scale.json")
            .addQueryParameter("day", date.day.toString())
            .addQueryParameter("month", (date.month + 1).toString())
            .addQueryParameter("year", date.year.toString())

        user?.let {
            builder.apply {
                addQueryParameter("login", it.name)
                addQueryParameter("password_hash", it.token)
            }
        }
        return builder.build()
    }

    private fun getPopularSankakuUrl(page: Int): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("posts")
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", limit.toString())

        val tags = "order:popular date:${date.getDateRangeString()} ${booru.getBlacklistsString()}".trim()
        if (isSafeMode && !query.contains(SAFE_MODE_TAG)) {
            builder.addQueryParameter("tags", "$tags $SAFE_MODE_TAG")
        } else {
            builder.addQueryParameter("tags", tags)
        }

        user?.let {
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

    fun getMoePostsUrl(page: Int) =
        if (pageType == PAGE_TYPE_POSTS) getPostsMoeUrl(page) else getPopularMoeUrl()

    fun getSankakuPostsUrl(page: Int) =
        if (pageType == PAGE_TYPE_POSTS) getPostsSankakuUrl(page) else getPopularSankakuUrl(page)
}