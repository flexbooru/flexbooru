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
import onlymash.flexbooru.api.url.MoeUrlHelper
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.entity.post.PostMoe
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.util.createStatusLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

/**
 * Moebooru posts data callback
 * This boundary callback gets notified when user reaches to the edges of the list such that the
 * database cannot provide any more data.
 * <p>
 * The boundary callback might be called multiple times for the same direction so it does its own
 * rate limiting using the PagingRequestHelper class.
 */
class PostMoeBoundaryCallback(
    private val moebooruApi: MoebooruApi,
    private val handleResponse: (Search, MutableList<PostMoe>?) -> Unit,
    private val ioExecutor: Executor,
    private val search: Search
) : PagedList.BoundaryCallback<PostMoe>() {

    //paging request helper
    val helper = PagingRequestHelper(ioExecutor)
    // network state
    val networkState = helper.createStatusLiveData()

    //last response posts size
    var lastResponseSize = search.limit

    private fun insertItemsIntoDb(response: Response<MutableList<PostMoe>>, it: PagingRequestHelper.Request.Callback) {
        ioExecutor.execute {
            val data = response.body()
            lastResponseSize = data?.size ?: 0
            handleResponse(search, data)
            it.recordSuccess()
        }
    }

    private fun createMoebooruCallback(it: PagingRequestHelper.Request.Callback)
            : Callback<MutableList<PostMoe>> {
        return object : Callback<MutableList<PostMoe>> {
            override fun onFailure(
                call: Call<MutableList<PostMoe>>,
                t: Throwable) {
                it.recordFailure(t)
            }

            override fun onResponse(
                call: Call<MutableList<PostMoe>>,
                response: Response<MutableList<PostMoe>>
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
            moebooruApi.getPosts(MoeUrlHelper.getPostUrl(search, 1)).enqueue(createMoebooruCallback(it))
        }
    }

    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: PostMoe) {
        val indexInNext = itemAtEnd.indexInResponse + 1
        val limit = search.limit
        if (lastResponseSize == limit) {
            helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
                moebooruApi.getPosts(MoeUrlHelper.getPostUrl(search, indexInNext/limit + 1))
                    .enqueue(createMoebooruCallback(it))
            }
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: PostMoe) {
        // ignored, since we only ever append to what's in the DB
    }
}