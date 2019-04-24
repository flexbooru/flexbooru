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

package onlymash.flexbooru.repository

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import java.io.IOException
import java.util.concurrent.Executor

/**
 * Base [PageKeyedDataSource] add [NetworkState] and retry [Executor]
 * */
abstract class BasePageKeyedDataSource<Key, Value>(
    private val retryExecutor: Executor
) : PageKeyedDataSource<Key, Value>() {

    // keep a function reference for the retry event
    private var retry: (() -> Any)? = null

    /**
     * There is no sync on the state because paging will always call loadInitial first then wait
     * for it to return some success value before calling loadAfter.
     */
    val networkState = MutableLiveData<NetworkState>()

    //initload network state
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

    //not more data
    fun onEnd() {
        networkState.postValue(NetworkState.LOADED)
    }

    //init request
    abstract fun loadInitialRequest(params: LoadInitialParams<Key>, callback: LoadInitialCallback<Key, Value>)

    override fun loadInitial(params: LoadInitialParams<Key>, callback: LoadInitialCallback<Key, Value>) {
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)
        try {
            loadInitialRequest(params, callback)
            retry = null
            networkState.postValue(NetworkState.LOADED)
            initialLoad.postValue(NetworkState.LOADED)
        } catch (ioException: IOException) {
            retry = {
                loadInitial(params, callback)
            }
            val error = NetworkState.error(ioException.message ?: "unknown error")
            networkState.postValue(error)
            initialLoad.postValue(error)
        }
    }

    //after request
    abstract fun loadAfterRequest(params: LoadParams<Key>, callback: LoadCallback<Key, Value>)

    override fun loadAfter(params: LoadParams<Key>, callback: LoadCallback<Key, Value>) {
        networkState.postValue(NetworkState.LOADING)
        loadAfterRequest(params, callback)
    }

    internal fun loadAfterOnFailed(msg: String, params: LoadParams<Key>, callback: LoadCallback<Key, Value>) {
        retry = {
            loadAfter(params, callback)
        }
        networkState.postValue(NetworkState.error(msg))
    }

    internal fun loadAfterOnSuccess() {
        retry = null
    }

    override fun loadBefore(params: LoadParams<Key>, callback: LoadCallback<Key, Value>) {

    }
}