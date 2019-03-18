/*
 * Copyright (C) 2019 by onlymash <im@fiepi.me>, All rights reserved
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
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.url.DanUrlHelper
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.entity.post.SearchPopular
import onlymash.flexbooru.entity.post.PostDan
import onlymash.flexbooru.repository.NetworkState
import java.io.IOException
import java.util.concurrent.Executor

//danbooru popular posts data source
class PopularDanDataSource(
    private val danbooruApi: DanbooruApi,
    private val db: FlexbooruDatabase,
    private val popular: SearchPopular,
    private val retryExecutor: Executor) : PageKeyedDataSource<Int, PostDan>() {

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
        prevRetry?.let {
            retryExecutor.execute {
                it.invoke()
            }
        }
    }

    override fun loadInitial(params: LoadInitialParams<Int>,
                             callback: LoadInitialCallback<Int, PostDan>) {
        val request = danbooruApi.getPosts(DanUrlHelper.getPopularUrl(popular))
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)

        val scheme = popular.scheme
        val host = popular.host
        val keyword = popular.scale

        // triggered by a refresh, we better execute sync
        try {
            val response = request.execute()
            if (response.code() == 401) {
                retry = {
                    loadInitial(params, callback)
                }
                val error = NetworkState.error("Your Api Key is wrong.")
                networkState.postValue(error)
                initialLoad.postValue(error)
                return
            }
            val data = response.body()?: mutableListOf()
            var posts: MutableList<PostDan> = mutableListOf()
            data.forEach { post ->
                if (!post.preview_file_url.isNullOrBlank()) {
                    posts.add(post)
                }
            }
            if (popular.safe_mode) {
                val tmp: MutableList<PostDan> = mutableListOf()
                posts.forEach {
                    if (it.rating == "s") {
                        tmp.add(it)
                    }
                }
                posts = tmp
            }
            db.postDanDao().deletePosts(host, keyword)
            val start = db.postDanDao().getNextIndex(host = host, keyword = keyword)
            val items = posts.mapIndexed { index, post ->
                post.scheme = scheme
                post.host = host
                post.keyword = keyword
                post.indexInResponse = start + index
                post
            }
            db.postDanDao().insert(items)
            retry = null
            networkState.postValue(NetworkState.LOADED)
            initialLoad.postValue(NetworkState.LOADED)
            callback.onResult(items, null, null)
        } catch (ioException: IOException) {
            retry = {
                loadInitial(params, callback)
            }
            val error = NetworkState.error(ioException.message ?: "unknown error")
            networkState.postValue(error)
            initialLoad.postValue(error)
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, PostDan>) {

    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, PostDan>) {

    }

}