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

package onlymash.flexbooru.repository.popular

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.api.SankakuApi
import onlymash.flexbooru.api.url.SankakuUrlHelper
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.entity.post.SearchPopular
import onlymash.flexbooru.entity.post.PostSankaku
import onlymash.flexbooru.extension.NetResult
import onlymash.flexbooru.repository.NetworkState

//sankaku popular posts data source
class PopularSankakuDataSource(
    private val scope: CoroutineScope,
    private val sankakuApi: SankakuApi,
    private val db: FlexbooruDatabase,
    private val popular: SearchPopular) : PageKeyedDataSource<Int, PostSankaku>() {

    // keep a function reference for the retry event
    private var retry: (() -> Any)? = null

    /**
     * There is no sync on the state because paging will always call loadInitial first then wait
     * for it to return some success value before calling loadAfter.
     */
    val networkState = MutableLiveData<NetworkState>()

    val initialLoad = MutableLiveData<NetworkState>()

    //retry failed request
    fun retryAllFailed() {
        val prevRetry = retry
        retry = null
        prevRetry?.invoke()
    }

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, PostSankaku>) {
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)
        val scheme = popular.scheme
        val host = popular.host
        val keyword = popular.scale
        scope.launch {
            when (val result = withContext(Dispatchers.IO) {
                try {
                    val response = sankakuApi.getPosts(SankakuUrlHelper.getPopularUrl(popular, 1))
                    var data = response.body()?: mutableListOf()
                    if (popular.safe_mode) {
                        val tmp: MutableList<PostSankaku> = mutableListOf()
                        data.forEach {
                            if (it.rating == "s") {
                                tmp.add(it)
                            }
                        }
                        data = tmp
                    }
                    db.postSankakuDao().deletePosts(host, keyword)
                    val start = db.postSankakuDao().getNextIndex(host = host, keyword = keyword)
                    val items = data.mapIndexed { index, post ->
                        post.scheme = scheme
                        post.host = host
                        post.keyword = keyword
                        post.indexInResponse = start + index
                        post
                    }
                    db.postSankakuDao().insert(items)
                    NetResult.Success(items)
                } catch (e: Exception) {
                    NetResult.Error(e.message ?: "unknown error")
                }
            }) {
                is NetResult.Error -> {
                    retry = {
                        loadInitial(params, callback)
                    }
                    val error = NetworkState.error(result.errorMsg)
                    networkState.postValue(error)
                    initialLoad.postValue(error)
                }
                is NetResult.Success -> {
                    retry = null
                    networkState.postValue(NetworkState.LOADED)
                    initialLoad.postValue(NetworkState.LOADED)
                    val items = result.data
                    if (items.size < popular.limit) {
                        callback.onResult(items, null, null)
                    } else {
                        callback.onResult(items, null, 2)
                    }
                }
            }
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, PostSankaku>) {
        networkState.postValue(NetworkState.LOADING)
        val page = params.key
        val scheme = popular.scheme
        val host = popular.host
        val keyword = popular.scale
        scope.launch {
            when (val result = withContext(Dispatchers.IO) {
                try {
                    val response = sankakuApi.getPosts(SankakuUrlHelper.getPopularUrl(popular, page))
                    var data = response.body()?: mutableListOf()
                    if (popular.safe_mode) {
                        val tmp: MutableList<PostSankaku> = mutableListOf()
                        data.forEach {
                            if (it.rating == "s") {
                                tmp.add(it)
                            }
                        }
                        data = tmp
                    }
                    val start = db.postSankakuDao().getNextIndex(host = host, keyword = keyword)
                    val items = data.mapIndexed { index, post ->
                        post.scheme = scheme
                        post.host = host
                        post.keyword = keyword
                        post.indexInResponse = start + index
                        post
                    }
                    db.postSankakuDao().insert(items)
                    NetResult.Success(items)
                } catch (e: Exception) {
                    NetResult.Error(e.message ?: "unknown error")
                }
            }) {
                is NetResult.Error -> {
                    retry = {
                        loadAfter(params, callback)
                    }
                    val error = NetworkState.error(result.errorMsg)
                    networkState.postValue(error)
                }
                is NetResult.Success -> {
                    retry = null
                    networkState.postValue(NetworkState.LOADED)
                    val items = result.data
                    if (items.size < popular.limit) {
                        callback.onResult(items, null)
                    } else {
                        callback.onResult(items, page + 1)
                    }
                }
            }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, PostSankaku>) {

    }
}