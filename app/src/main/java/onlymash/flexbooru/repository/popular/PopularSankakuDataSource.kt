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

import onlymash.flexbooru.api.SankakuApi
import onlymash.flexbooru.api.url.SankakuUrlHelper
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.entity.post.SearchPopular
import onlymash.flexbooru.entity.post.PostSankaku
import onlymash.flexbooru.repository.BasePageKeyedDataSource
import onlymash.flexbooru.repository.NetworkState
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.Executor

//sankaku popular posts data source
class PopularSankakuDataSource(
    private val sankakuApi: SankakuApi,
    private val db: FlexbooruDatabase,
    private val popular: SearchPopular,
    retryExecutor: Executor) : BasePageKeyedDataSource<Int, PostSankaku>(retryExecutor) {

    override fun loadInitialRequest(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, PostSankaku>) {
        val request = sankakuApi.getPosts(SankakuUrlHelper.getPopularUrl(popular, 1))
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)
        val scheme = popular.scheme
        val host = popular.host
        val keyword = popular.scale
        val response = request.execute()
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
        if (items.size < popular.limit) {
            callback.onResult(items, null, null)
            onEnd()
        } else {
            callback.onResult(items, null, 2)
        }
    }

    override fun loadAfterRequest(params: LoadParams<Int>, callback: LoadCallback<Int, PostSankaku>) {
        val page = params.key
        sankakuApi.getPosts(SankakuUrlHelper.getPopularUrl(popular, page))
            .enqueue(object : retrofit2.Callback<MutableList<PostSankaku>> {
                override fun onFailure(call: Call<MutableList<PostSankaku>>, t: Throwable) {
                    loadAfterOnFailed(t.message ?: "unknown err", params, callback)
                }

                override fun onResponse(
                    call: Call<MutableList<PostSankaku>>,
                    response: Response<MutableList<PostSankaku>>
                ) {
                    if (response.isSuccessful) {
                        val scheme = popular.scheme
                        val host = popular.host
                        val keyword = popular.scale
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
                        loadAfterOnSuccess()
                        if (items.size < popular.limit) {
                            callback.onResult(items, null)
                            onEnd()
                        } else {
                            callback.onResult(items, page + 1)
                        }
                    } else {
                        loadAfterOnFailed("error code: ${response.code()}", params, callback)
                    }
                }
            })
    }
}