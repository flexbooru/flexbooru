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

package onlymash.flexbooru.data.repository.comment

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.common.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.common.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.data.action.ActionComment
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.model.common.Comment
import onlymash.flexbooru.data.repository.NetworkState
import onlymash.flexbooru.extension.NetResult

class CommentDataSource(
    private val action: ActionComment,
    private val booruApis: BooruApis,
    private val scope: CoroutineScope) : PageKeyedDataSource<Int, Comment>() {

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
        callback: LoadInitialCallback<Int, Comment>
    ) {
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)
        scope.launch {
            when(val result = when(action.booru.type) {
                BOORU_TYPE_DAN -> getDanComments(action, 1)
                BOORU_TYPE_DAN1 -> getDan1Comments(action, 1)
                BOORU_TYPE_MOE -> getMoeComments(action, 1)
                BOORU_TYPE_GEL -> getGelComments(action, 0)
                else -> getSankakuComments(action, 1)
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
                    if (result.data.size < action.limit) {
                        callback.onResult(result.data, null, null)
                    } else {
                        callback.onResult(result.data, null, 2)
                    }
                }
            }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Comment>) {

    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Comment>) {
        networkState.postValue(NetworkState.LOADING)
        val page = params.key
        scope.launch {
            when(val result = when(action.booru.type) {
                BOORU_TYPE_DAN -> getDanComments(action, page)
                BOORU_TYPE_DAN1 -> getDan1Comments(action,  page)
                BOORU_TYPE_MOE -> getMoeComments(action, page)
                BOORU_TYPE_GEL -> getGelComments(action, page)
                else -> getSankakuComments(action, page)
            }) {
                is NetResult.Error -> {
                    retry = {
                        loadAfter(params, callback)
                    }
                    networkState.postValue(NetworkState.error(result.errorMsg))
                }
                is NetResult.Success -> {
                    retry = null
                    networkState.postValue(NetworkState.LOADED)
                    if (result.data.size < action.limit) {
                        callback.onResult(result.data, null)
                    } else {
                        callback.onResult(result.data, page + 1)
                    }
                }
            }
        }
    }

    private fun ActionComment.getDanUrl(page: Int): HttpUrl =
        if (postId > 0) getDanPostCommentUrl(page) else getDanPostsCommentUrl(page)

    private fun ActionComment.getDan1Url(page: Int): HttpUrl =
        when {
            postId > 0 -> getDan1PostCommentUrl(page)
            query.isNotEmpty() -> getDan1PostsCommentSearchUrl(page)
            else -> getDan1PostsCommentIndexUrl(page)
        }

    private fun ActionComment.getMoeUrl(page: Int): HttpUrl =
        when {
            postId > 0 -> getMoePostCommentUrl(page)
            else -> getMoePostsCommentUrl(page)
        }

    private fun ActionComment.getGelUrl(page: Int): HttpUrl =
        if (postId > 0) getGelPostCommentUrl(page) else getGelPostsCommentUrl(page)

    private fun ActionComment.getSankakuUrl(page: Int): HttpUrl {
        return when {
            postId > 0 -> getSankakuPostCommentUrl(page)
            query.isNotBlank() -> getSankakuPostsCommentSearchUrl(page)
            else -> getSankakuPostsCommentIndexUrl(page)
        }
    }

    private suspend fun getDanComments(action: ActionComment, page: Int): NetResult<List<Comment>> {
        return withContext(Dispatchers.IO) {
            try {
                val response =  booruApis.danApi.getComments(action.getDanUrl(page))
                if (response.isSuccessful) {
                    val pools = response.body()?.map { it.toComment() } ?: listOf()
                    NetResult.Success(pools)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun getDan1Comments(action: ActionComment, page: Int): NetResult<List<Comment>> {
        return withContext(Dispatchers.IO) {
            try {
                val response =  booruApis.dan1Api.getComments(action.getDan1Url(page))
                if (response.isSuccessful) {
                    val pools = response.body()?.map { it.toComment() } ?: listOf()
                    NetResult.Success(pools)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun getMoeComments(action: ActionComment, page: Int): NetResult<List<Comment>> {
        return withContext(Dispatchers.IO) {
            try {
                val response =  booruApis.moeApi.getComments(action.getMoeUrl(page))
                if (response.isSuccessful) {
                    val pools = response.body()?.map { it.toComment() } ?: listOf()
                    NetResult.Success(pools)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun getSankakuComments(action: ActionComment, page: Int): NetResult<List<Comment>> {
        return withContext(Dispatchers.IO) {
            try {
                val response =  booruApis.sankakuApi.getComments(action.getSankakuUrl(page))
                if (response.isSuccessful) {
                    val pools = response.body()?.map { it.toComment() } ?: listOf()
                    NetResult.Success(pools)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun getGelComments(action: ActionComment, page: Int): NetResult<List<Comment>> {
        return withContext(Dispatchers.IO) {
            try {
                val response =  booruApis.gelApi.getComments(action.getGelUrl(page))
                if (response.isSuccessful) {
                    val pools = response.body()?.comments?.map { it.toComment() } ?: listOf()
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