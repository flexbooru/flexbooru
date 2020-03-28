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

package onlymash.flexbooru.data.repository.artist

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.data.action.ActionArtist
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.model.common.Artist
import onlymash.flexbooru.data.repository.NetworkState
import onlymash.flexbooru.extension.NetResult

class ArtistDataSource(
    private val action: ActionArtist,
    private val booruApis: BooruApis,
    private val scope: CoroutineScope) : PageKeyedDataSource<Int, Artist>() {

    private var retry:(() -> Any)? = null

    val networkState = MutableLiveData<NetworkState>()

    val initialLoad = MutableLiveData<NetworkState>()

    fun retryAllFailed() {
        val prevRetry = retry
        retry = null
        prevRetry?.let { pre ->
            scope.launch(Dispatchers.IO) {
                pre.invoke()
            }
        }
    }

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Artist>
    ) {
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)
        scope.launch {
            when(val result = getArtists(action, 1)) {
                is NetResult.Error -> {
                    retry = {
                        loadInitial(params, callback)
                    }
                    val error = NetworkState.error(result.errorMsg)
                    networkState.postValue(error)
                    initialLoad.postValue(error)
                }
                is NetResult.Success -> {
                    if (result.data.size < action.limit) {
                        callback.onResult(result.data, null, null)
                    } else {
                        callback.onResult(result.data, null, 2)
                    }
                    retry = null
                    networkState.postValue(NetworkState.LOADED)
                    initialLoad.postValue(NetworkState.LOADED)
                }
            }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Artist>) {

    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Artist>) {
        networkState.postValue(NetworkState.LOADING)
        val page = params.key
        scope.launch {
            when(val result = getArtists(action, page)) {
                is NetResult.Error -> {
                    retry = {
                        loadAfter(params, callback)
                    }
                    networkState.postValue(NetworkState.error(result.errorMsg))
                }
                is NetResult.Success -> {
                    if (result.data.size < action.limit) {
                        callback.onResult(result.data, null)
                    } else {
                        callback.onResult(result.data, page + 1)
                    }
                    retry = null
                    networkState.postValue(NetworkState.LOADED)
                }
            }
        }
    }

    private suspend fun getArtists(action: ActionArtist, page: Int): NetResult<List<Artist>> {
        return withContext(Dispatchers.IO) {
            try {
                val response =  when (action.booru.type) {
                    BOORU_TYPE_DAN -> booruApis.danApi.getArtists(action.getDanArtistsUrl(page))
                    BOORU_TYPE_DAN1 -> booruApis.dan1Api.getArtists(action.getDan1ArtistsUrl(page))
                    else -> booruApis.moeApi.getArtists(action.getMoeArtistsUrl(page))
                }
                if (response.isSuccessful) {
                    val pools = response.body() ?: listOf()
                    NetResult.Success(pools)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }
}