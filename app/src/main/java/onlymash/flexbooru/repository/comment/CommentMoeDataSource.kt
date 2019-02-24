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

package onlymash.flexbooru.repository.comment

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import onlymash.flexbooru.api.ApiUrlHelper
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.entity.CommentAction
import onlymash.flexbooru.entity.CommentMoe
import onlymash.flexbooru.repository.NetworkState
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.Executor

class CommentMoeDataSource(private val moebooruApi: MoebooruApi,
                           private val commentAction: CommentAction,
                           private val retryExecutor: Executor
) : PageKeyedDataSource<Int, CommentMoe>() {

    // keep a function reference for the retry event
    private var retry: (() -> Any)? = null

    /**
     * There is no sync on the state because paging will always call loadInitial first then wait
     * for it to return some success value before calling loadAfter.
     */
    val networkState = MutableLiveData<NetworkState>()

    val initialLoad = MutableLiveData<NetworkState>()

    fun retryAllFailed() {
        val prevRetry = retry
        retry = null
        prevRetry?.let {
            retryExecutor.execute {
                it.invoke()
            }
        }
    }

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, CommentMoe>) {
        val request = moebooruApi.getComments(
            url = ApiUrlHelper.getMoePostsCommentUrl(
                commentAction = commentAction,
                page = 1))
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)
        val scheme = commentAction.scheme
        val host = commentAction.host
        try {
            val response = request.execute()
            val data = response.body() ?: mutableListOf()
            data.forEach {
                it.scheme = scheme
                it.host = host
            }
            retry = null
            networkState.postValue(NetworkState.LOADED)
            initialLoad.postValue(NetworkState.LOADED)
            callback.onResult(data, null, 2)
        }  catch (ioException: IOException) {
            retry = {
                loadInitial(params, callback)
            }
            val error = NetworkState.error(ioException.message ?: "unknown error")
            networkState.postValue(error)
            initialLoad.postValue(error)
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, CommentMoe>) {
        networkState.postValue(NetworkState.LOADING)
        val page = params.key
        moebooruApi.getComments(ApiUrlHelper.getMoePostsCommentUrl(commentAction, page))
            .enqueue(object : retrofit2.Callback<MutableList<CommentMoe>> {
                override fun onFailure(call: Call<MutableList<CommentMoe>>, t: Throwable) {
                    retry = {
                        loadAfter(params, callback)
                    }
                    networkState.postValue(NetworkState.error(t.message ?: "unknown err"))
                }

                override fun onResponse(
                    call: Call<MutableList<CommentMoe>>,
                    response: Response<MutableList<CommentMoe>>
                ) {
                   if (response.isSuccessful) {
                       val data = response.body() ?: mutableListOf()
                       val scheme = commentAction.scheme
                       val host = commentAction.host
                       data.forEach {
                           it.scheme = scheme
                           it.host = host
                       }
                       retry = null
                       callback.onResult(data, page + 1)
                   } else {
                       retry = {
                           loadAfter(params, callback)
                       }
                       networkState.postValue(NetworkState.error("error code: ${response.code()}"))
                   }
                }

            })
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, CommentMoe>) {

    }
}