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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.api.url.GelUrlHelper
import onlymash.flexbooru.api.GelbooruApi
import onlymash.flexbooru.entity.post.PostGel
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.entity.common.TagBlacklist
import onlymash.flexbooru.entity.post.PostGelResponse
import onlymash.flexbooru.extension.NetResult
import onlymash.flexbooru.extension.createStatusLiveData
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
    private val scope: CoroutineScope,
    private val gelbooruApi: GelbooruApi,
    private val handleResponse: (Search, MutableList<PostGel>?, MutableList<TagBlacklist>) -> Unit,
    private val search: Search,
    private val tagBlacklists: MutableList<TagBlacklist>,
    ioExecutor: Executor
) : PagedList.BoundaryCallback<PostGel>() {

    //paging request helper
    val helper = PagingRequestHelper(ioExecutor)
    // network state
    val networkState = helper.createStatusLiveData()

    //last response posts size
    var lastResponseSize = search.limit

    private suspend fun insertItemsIntoDb(response: Response<PostGelResponse>, it: PagingRequestHelper.Request.Callback) {
        withContext(Dispatchers.IO) {
            it.recordSuccess()
            val data = response.body()?.posts
            lastResponseSize = data?.size ?: 0
            handleResponse(search, data, tagBlacklists)
        }
    }

    private fun createCallback(page: Int, it: PagingRequestHelper.Request.Callback) {
        scope.launch {
            when (val result = withContext(Dispatchers.IO) {
                try {
                    val response = gelbooruApi.getPosts(GelUrlHelper.getPostUrl(search, page))
                    NetResult.Success(response)
                } catch (e: Exception) {
                    NetResult.Error(e.message.toString())
                }
            }) {
                is NetResult.Success -> {
                    insertItemsIntoDb(result.data, it)
                }
                is NetResult.Error -> {
                    it.recordFailure(Throwable(result.errorMsg))
                }
            }
        }
    }

    /**
     * Database returned 0 items. We should query the backend for more items.
     */
    @MainThread
    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            createCallback(0, it)
        }
    }

    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: PostGel) {
        val indexInNext = itemAtEnd.indexInResponse + 1
        val limit = search.limit
        if (lastResponseSize == limit) {
            helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
                createCallback(indexInNext/limit, it)
            }
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: PostGel) {
        // ignored, since we only ever append to what's in the DB
    }
}