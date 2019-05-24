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
import onlymash.flexbooru.api.SankakuApi
import onlymash.flexbooru.api.url.SankakuUrlHelper
import onlymash.flexbooru.entity.post.PostSankaku
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.entity.TagBlacklist
import onlymash.flexbooru.extension.NetResult
import onlymash.flexbooru.extension.createStatusLiveData
import retrofit2.Response
import java.util.concurrent.Executor

/**
 * Sankaku posts data callback
 * This boundary callback gets notified when user reaches to the edges of the list such that the
 * database cannot provide any more data.
 * <p>
 * The boundary callback might be called multiple times for the same direction so it does its own
 * rate limiting using the PagingRequestHelper class.
 */
class PostSankakuBoundaryCallback(
    private val scope: CoroutineScope,
    private val sankakuApi: SankakuApi,
    private val handleResponse: (Search, MutableList<PostSankaku>?, MutableList<TagBlacklist>) -> Unit,
    private val search: Search,
    private val tagBlacklists: MutableList<TagBlacklist>,
    ioExecutor: Executor
) : PagedList.BoundaryCallback<PostSankaku>() {

    //PagingRequestHelper
    val helper = PagingRequestHelper(ioExecutor)
    //network state
    val networkState = helper.createStatusLiveData()

    //last response posts size
    var lastResponseSize = search.limit

    private suspend fun insertItemsIntoDb(response: Response<MutableList<PostSankaku>>, it: PagingRequestHelper.Request.Callback) {
        withContext(Dispatchers.IO) {
            val data = response.body()
            lastResponseSize = data?.size ?: 0
            handleResponse(search, data, tagBlacklists)
            it.recordSuccess()
        }
    }

    private fun createCallback(page: Int, it: PagingRequestHelper.Request.Callback) {
        scope.launch {
            when (val result = withContext(Dispatchers.IO) {
                try {
                    val response = sankakuApi.getPosts(SankakuUrlHelper.getPostUrl(search, page)).execute()
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
            createCallback(1, it)
        }
    }

    /**
     * User reached to the end of the list.
     */
    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: PostSankaku) {
        val indexInNext = itemAtEnd.indexInResponse + 1
        val limit = search.limit
        if (lastResponseSize == limit) {
            helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
                createCallback(indexInNext/limit + 1, it)
            }
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: PostSankaku) {
        // ignored, since we only ever append to what's in the DB
    }
}