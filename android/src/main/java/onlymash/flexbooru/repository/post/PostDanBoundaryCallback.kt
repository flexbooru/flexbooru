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
import onlymash.flexbooru.common.App
import onlymash.flexbooru.R
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.url.DanUrlHelper
import onlymash.flexbooru.entity.post.PostDan
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.entity.common.TagBlacklist
import onlymash.flexbooru.extension.NetResult
import onlymash.flexbooru.extension.createStatusLiveData
import retrofit2.Response
import java.util.concurrent.Executor

/**
 * Danbooru posts data callback
 * This boundary callback gets notified when user reaches to the edges of the list such that the
 * database cannot provide any more data.
 * <p>
 * The boundary callback might be called multiple times for the same direction so it does its own
 * rate limiting using the PagingRequestHelper class.
 */
class PostDanBoundaryCallback(
    private val scope: CoroutineScope,
    private val danbooruApi: DanbooruApi,
    private val handleResponse: (Search, MutableList<PostDan>?, MutableList<TagBlacklist>) -> Unit,
    private val search: Search,
    private val tagBlacklists: MutableList<TagBlacklist>,
    ioExecutor: Executor
) : PagedList.BoundaryCallback<PostDan>() {

    //PagingRequestHelper
    val helper = PagingRequestHelper(ioExecutor)
    //network state
    val networkState = helper.createStatusLiveData()

    //last response posts size
    var lastResponseSize = search.limit

    private suspend fun insertItemsIntoDb(response: Response<MutableList<PostDan>>, it: PagingRequestHelper.Request.Callback) {
        withContext(Dispatchers.IO) {
            it.recordSuccess()
            val data = response.body()
            lastResponseSize = data?.size ?: 0
            handleResponse(search, data, tagBlacklists)
        }
    }

    private fun createCallback(page: Int, it: PagingRequestHelper.Request.Callback) {
        scope.launch {
            when (val result = withContext(Dispatchers.IO) {
                try {
                    val response = danbooruApi.getPosts(DanUrlHelper.getPostUrl(search, page))
                    NetResult.Success(response)
                } catch (e: Exception) {
                    NetResult.Error(e.message.toString())
                }
            }) {
                is NetResult.Success -> {
                    val response = result.data
                    if (response.code() == 401) {
                        it.recordFailure(Throwable(App.app.getString(R.string.msg_your_api_key_is_wrong)))
                    } else {
                        insertItemsIntoDb(response, it)
                    }
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
    override fun onItemAtEndLoaded(itemAtEnd: PostDan) {
        val indexInNext = itemAtEnd.indexInResponse + 1
        val limit = search.limit
        if (lastResponseSize == limit) {
            helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
                createCallback(indexInNext/limit + 1, it)
            }
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: PostDan) {
        // ignored, since we only ever append to what's in the DB
    }
}