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
import onlymash.flexbooru.R
import onlymash.flexbooru.api.HydrusApi
import onlymash.flexbooru.api.url.HydrusUrlHelper
import onlymash.flexbooru.common.App
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.entity.common.TagBlacklist
import onlymash.flexbooru.entity.post.PostHydrusFileId
import onlymash.flexbooru.entity.post.PostHydrusFileResponse
import onlymash.flexbooru.extension.NetResult
import onlymash.flexbooru.extension.createStatusLiveData
import retrofit2.Response
import java.util.concurrent.Executor

/**
 * Hydrus posts data callback
 * This boundary callback gets notified when user reaches to the edges of the list such that the
 * database cannot provide any more data.
 * <p>
 * The boundary callback might be called multiple times for the same direction so it does its own
 * rate limiting using the PagingRequestHelper class.
 */
class PostHydrusBoundaryCallback(
    private val scope: CoroutineScope,
    private val hydrusApi: HydrusApi,
    private val handleResponse: (Search, MutableList<PostHydrusFileResponse>, MutableList<TagBlacklist>) -> Unit,
    private val search: Search,
    private val tagBlacklists: MutableList<TagBlacklist>,
    ioExecutor: Executor
) : PagedList.BoundaryCallback<PostHydrusFileResponse>() {

    //PagingRequestHelper
    val helper = PagingRequestHelper(ioExecutor)
    //network state
    val networkState = helper.createStatusLiveData()

    //last response posts size
    var lastResponseSize = search.limit

    private suspend fun insertItemsIntoDb(
        response: Response<PostHydrusFileId>,
        it: PagingRequestHelper.Request.Callback
    ) {
        withContext(Dispatchers.IO) {
            val data = response.body()
            lastResponseSize = 0

            var lists = mutableListOf<PostHydrusFileResponse>()
            var postsHydrus: MutableList<Int>
            var hydrusIdList = response.body()
            var listHydrus = mutableListOf<PostHydrusFileResponse>()
            if (hydrusIdList != null) {
                var arrayIds = hydrusIdList.file_ids
                postsHydrus = arrayIds.toMutableList()


                var i = 0
                for (post in postsHydrus) {
                    var response = PostHydrusFileResponse(
                        id = post,
                        height = 800,
                        width = 800,
                        score = "0",
                        file_url ="http://${search.host}/get_files/file?Hydrus-Client-API-Access-Key=${search.auth_key}&file_id=$post",
                        sample_url = "http://${search.host}/get_files/file?Hydrus-Client-API-Access-Key=${search.auth_key}&file_id=$post",
                        preview_url= "http://${search.host}/get_files/thumbnail?Hydrus-Client-API-Access-Key=${search.auth_key}&file_id=$post",
                        sample_height = 800,
                        sample_width = 800,
                        rating = "explicit",
                        tags = "Hydrus",
                        change = 1L,
                        md5 = "",
                        creator_id = post,
                        has_children = false,
                        created_at = "2019",
                        status = "",
                        source = "",
                        preview_height = 800,
                        has_comments = false,
                        has_notes = false,
                        preview_width = 800
                    )
                    response.keyword = "test"
                    response.scheme = "http"
                    response.uid = hydrusIdList.uid
                    response.host = search.host
                    response.indexInResponse = i

                    listHydrus.add(response)
                    i++
                }
            }
            handleResponse(search, listHydrus,tagBlacklists)
            it.recordSuccess()
        }
    }

    private fun createCallback(page: Int, it: PagingRequestHelper.Request.Callback) {
        scope.launch {
            when (val result = withContext(Dispatchers.IO) {
                try {
                    val response = hydrusApi.getPosts(HydrusUrlHelper.getPostUrl(search, page))
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
    override fun onItemAtEndLoaded(itemAtEnd: PostHydrusFileResponse) {
        val indexInNext = itemAtEnd.indexInResponse + 1
        val limit = search.limit
        if (lastResponseSize == limit) {
            helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
                createCallback(indexInNext / limit + 1, it)
            }
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: PostHydrusFileResponse) {
        // ignored, since we only ever append to what's in the DB
    }

    //hydrus
}