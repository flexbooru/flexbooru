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

package onlymash.flexbooru.repository.post

import androidx.annotation.MainThread
import androidx.paging.PagedList
import androidx.paging.PagingRequestHelper
import onlymash.flexbooru.api.url.GelUrlHelper
import onlymash.flexbooru.api.GelbooruApi
import onlymash.flexbooru.entity.post.PostGel
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.entity.post.PostGelResponse
import onlymash.flexbooru.util.createStatusLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

/**
 * Gelbooru posts data callback
 * This boundary callback gets notified when user reaches to the edges of the list such that the
 * database cannot provide any more data.
 * <p>
 * The boundary callback might be called multiple times for the same direction so it does its own
 * rate limiting using the PagingRequestHelper class.
 */
class PostGelBoundaryCallback(
    private val gelbooruApi: GelbooruApi,
    private val handleResponse: (Search, MutableList<PostGel>?) -> Unit,
    private val ioExecutor: Executor,
    private val search: Search
) : PagedList.BoundaryCallback<PostGel>() {

    //paging request helper
    val helper = PagingRequestHelper(ioExecutor)
    // network state
    val networkState = helper.createStatusLiveData()

    //last response posts size
    var lastResponseSize = search.limit

    private fun insertItemsIntoDb(response: Response<PostGelResponse>, it: PagingRequestHelper.Request.Callback) {
        ioExecutor.execute {
            val data = response.body()?.posts
            lastResponseSize = data?.size ?: 0
            handleResponse(search, data)
            it.recordSuccess()
        }
    }

    private fun createGelbooruCallback(it: PagingRequestHelper.Request.Callback)
            : Callback<PostGelResponse> {
        return object : Callback<PostGelResponse> {
            override fun onFailure(
                call: Call<PostGelResponse>,
                t: Throwable) {
                it.recordFailure(t)
            }

            override fun onResponse(
                call: Call<PostGelResponse>,
                response: Response<PostGelResponse>
            ) {
                insertItemsIntoDb(response, it)
            }
        }
    }

    /**
     * Database returned 0 items. We should query the backend for more items.
     */
    @MainThread
    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            gelbooruApi.getPosts(GelUrlHelper.getPostUrl(search, 1)).enqueue(createGelbooruCallback(it))
        }
    }

    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: PostGel) {
        val indexInNext = itemAtEnd.indexInResponse + 1
        val limit = search.limit
        if (lastResponseSize == limit) {
            helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
                gelbooruApi.getPosts(GelUrlHelper.getPostUrl(search, indexInNext/limit + 1))
                    .enqueue(createGelbooruCallback(it))
            }
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: PostGel) {
        // ignored, since we only ever append to what's in the DB
    }
}