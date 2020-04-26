/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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

package onlymash.flexbooru.data.repository.tag

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.app.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.data.action.ActionTag
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.model.common.Tag
import onlymash.flexbooru.data.repository.NetworkState
import onlymash.flexbooru.extension.NetResult

class TagDataSource(
    private val action: ActionTag,
    private val booruApis: BooruApis,
    private val scope: CoroutineScope) : PageKeyedDataSource<Int, Tag>() {

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
        callback: LoadInitialCallback<Int, Tag>
    ) {
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)
        scope.launch {
            when(val result = when(action.booru.type) {
                BOORU_TYPE_DAN -> getDanTags(action, 1)
                BOORU_TYPE_DAN1 -> getDan1Tags(action, 1)
                BOORU_TYPE_MOE -> getMoeTags(action, 1)
                BOORU_TYPE_GEL -> getGelTags(action, 0)
                else -> getSankakuTags(action, 1)
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
                    if (result.data.size < action.limit) {
                        callback.onResult(result.data, null, null)
                    } else {
                        callback.onResult(result.data, null, 2)
                    }
                    networkState.postValue(NetworkState.LOADED)
                    initialLoad.postValue(NetworkState.LOADED)
                }
            }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Tag>) {

    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Tag>) {
        networkState.postValue(NetworkState.LOADING)
        val page = params.key
        scope.launch {
            when(val result = when(action.booru.type) {
                BOORU_TYPE_DAN -> getDanTags(action, page)
                BOORU_TYPE_DAN1 -> getDan1Tags(action,  page)
                BOORU_TYPE_MOE -> getMoeTags(action, page)
                BOORU_TYPE_GEL -> getGelTags(action, page)
                else -> getSankakuTags(action, page)
            }) {
                is NetResult.Error -> {
                    retry = {
                        loadAfter(params, callback)
                    }
                    networkState.postValue(NetworkState.error(result.errorMsg))
                }
                is NetResult.Success -> {
                    retry = null
                    if (result.data.size < action.limit) {
                        callback.onResult(result.data, null)
                    } else {
                        callback.onResult(result.data, page + 1)
                    }
                    networkState.postValue(NetworkState.LOADED)
                }
            }
        }
    }

    private suspend fun getDanTags(action: ActionTag, page: Int): NetResult<List<Tag>> {
        return withContext(Dispatchers.IO) {
            try {
                val response =  booruApis.danApi.getTags(action.getDanTagsUrl(page))
                if (response.isSuccessful) {
                    val pools = response.body()?.map { it.toTag() } ?: listOf()
                    NetResult.Success(pools)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun getDan1Tags(action: ActionTag, page: Int): NetResult<List<Tag>> {
        return withContext(Dispatchers.IO) {
            try {
                val response =  booruApis.dan1Api.getTags(action.getDan1TagsUrl(page))
                if (response.isSuccessful) {
                    val pools = response.body()?.map { it.toTag() } ?: listOf()
                    NetResult.Success(pools)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun getMoeTags(action: ActionTag, page: Int): NetResult<List<Tag>> {
        return withContext(Dispatchers.IO) {
            try {
                val response =  booruApis.moeApi.getTags(action.getMoeTagsUrl(page))
                if (response.isSuccessful) {
                    val pools = response.body()?.map { it.toTag() } ?: listOf()
                    NetResult.Success(pools)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun getSankakuTags(action: ActionTag, page: Int): NetResult<List<Tag>> {
        return withContext(Dispatchers.IO) {
            try {
                val response =  booruApis.sankakuApi.getTags(action.getSankakuTagsUrl(page))
                if (response.isSuccessful) {
                    val pools = response.body()?.map { it.toTag() } ?: listOf()
                    NetResult.Success(pools)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun getGelTags(action: ActionTag, page: Int): NetResult<List<Tag>> {
        return withContext(Dispatchers.IO) {
            try {
                val response =  booruApis.gelApi.getTags(action.getGelTagsUrl(page))
                if (response.isSuccessful) {
                    val pools = response.body()?.tags?.map { it.toTag() } ?: listOf()
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