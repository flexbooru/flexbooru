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
import onlymash.flexbooru.api.url.MoeUrlHelper
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.entity.post.PostMoe
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.entity.common.TagBlacklist
import onlymash.flexbooru.extension.NetResult
import onlymash.flexbooru.extension.createStatusLiveData
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
    private val scope: CoroutineScope,
    private val moebooruApi: MoebooruApi,
    private val handleResponse: (Search, MutableList<PostMoe>?) -> Unit,
    private val search: Search,
    tagBlacklists: MutableList<TagBlacklist>,
    ioExecutor: Executor
) : PagedList.BoundaryCallback<PostMoe>() {

    var tags = ""

    init {
        tagBlacklists.forEach { tagBlacklist ->
            tags = "$tags -${tagBlacklist.tag}"
        }
        tags = "${search.keyword}$tags".trim()
    }

    //paging request helper
    val helper = PagingRequestHelper(ioExecutor)
    // network state
    val networkState = helper.createStatusLiveData()

    //last response posts size
    var lastResponseSize = search.limit

    private suspend fun insertItemsIntoDb(response: Response<MutableList<PostMoe>>, it: PagingRequestHelper.Request.Callback) {
        withContext(Dispatchers.IO) {
            it.recordSuccess()
            val data = response.body()
            lastResponseSize = data?.size ?: 0
            handleResponse(search, data)
        }
    }

    private fun createCallback(page: Int, it: PagingRequestHelper.Request.Callback) {
        scope.launch {
            when (val result = withContext(Dispatchers.IO) {
                try {
                    val response = moebooruApi.getPosts(MoeUrlHelper.getPostUrl(search, page, tags))
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

    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: PostMoe) {
        val indexInNext = itemAtEnd.indexInResponse + 1
        val limit = search.limit
        if (lastResponseSize == limit) {
            helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
                createCallback(indexInNext/limit + 1, it)
            }
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: PostMoe) {
        // ignored, since we only ever append to what's in the DB
    }
}