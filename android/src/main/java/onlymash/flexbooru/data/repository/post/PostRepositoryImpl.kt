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

package onlymash.flexbooru.data.repository.post

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.toLiveData
import androidx.room.withTransaction
import kotlinx.coroutines.*
import onlymash.flexbooru.app.Values.BOORU_TYPE_SHIMMIE
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.app.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.action.ActionPost
import onlymash.flexbooru.data.api.DanbooruApi
import onlymash.flexbooru.data.database.MyDatabase
import onlymash.flexbooru.data.database.NextManager
import onlymash.flexbooru.data.model.common.Next
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.data.repository.Listing
import onlymash.flexbooru.data.repository.NetworkState
import onlymash.flexbooru.extension.NetResult

class PostRepositoryImpl(
    private val db: MyDatabase,
    private val booruApis: BooruApis
) : PostRepository {

    private var postBoundaryCallback: PostBoundaryCallback? = null

    private fun insertResultIntoDb(posts: List<Post>) {
        try {
            db.postDao().insert(posts)
        } catch (_: SQLiteConstraintException) {}
    }

    override fun getPosts(
        scope: CoroutineScope,
        action: ActionPost
    ): Listing<Post> {
        postBoundaryCallback = PostBoundaryCallback(
            action = action,
            booruApis = booruApis,
            scope = scope,
            handleResponse = this::insertResultIntoDb
        )
        val refreshTrigger = MutableLiveData<Unit?>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refreshPosts(action, scope)
        }
        val livePagedList = db.postDao()
            .getPosts(booruUid = action.booru.uid, query = action.query)
            .toLiveData(
                config = Config(
                    pageSize = action.limit,
                    maxSize = 300,
                    initialLoadSizeHint = action.limit * 2,
                    enablePlaceholders = true
                ),
                boundaryCallback = postBoundaryCallback,
                fetchExecutor = Dispatchers.IO.asExecutor()
            )
        return Listing(
            pagedList = livePagedList,
            networkState = postBoundaryCallback!!.networkState,
            retry = {
                scope.launch {
                    postBoundaryCallback!!.helper.retryAllFailed()
                }
            },
            refresh = { refreshTrigger.value = null },
            refreshState = refreshState
        )
    }

    private fun refreshPosts(
        action: ActionPost,
        scope: CoroutineScope
    ): LiveData<NetworkState> {
        postBoundaryCallback?.lastResponseSize = action.limit
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        scope.launch {
            when(val result = when(action.booru.type) {
                BOORU_TYPE_DAN -> {
                    if (action.booru.host in DanbooruApi.E621_HOSTS) {
                        refreshE621(action)
                    } else {
                        refreshDan(action)
                    }
                }
                BOORU_TYPE_DAN1 -> refreshDan1(action)
                BOORU_TYPE_MOE -> refreshMoe(action)
                BOORU_TYPE_GEL -> refreshGel(action)
                BOORU_TYPE_SHIMMIE -> refreshShimmie(action)
                else -> refreshSankaku(action)
            }) {
                is NetResult.Error -> {
                    networkState.value = NetworkState.error(result.errorMsg)
                }
                is NetResult.Success -> {
                    db.withTransaction {
                        db.postDao().deletePosts(booruUid = action.booru.uid, query = action.query)
                        insertResultIntoDb(result.data)
                    }
                    networkState.postValue(NetworkState.LOADED)
                }
            }
        }
        return networkState
    }

    private suspend fun refreshDan(action: ActionPost): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.danApi.getPosts(action.getDanPostsUrl(1))
                if (response.isSuccessful) {
                    val posts = (response.body()?.mapIndexed { index, post ->
                        post.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = index
                        )
                    } ?: listOf()).toMutableList()
                    posts.removeIf { it.id == -1 }
                    NetResult.Success(posts)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun refreshE621(action: ActionPost): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.danApi.getPostsE621(action.getDanPostsUrl(1))
                if (response.isSuccessful) {
                    val posts = response.body()?.posts?.mapIndexed { index, post ->
                        post.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            index = index
                        )
                    } ?: listOf()
                    NetResult.Success(posts)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun refreshDan1(action: ActionPost): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.dan1Api.getPosts(action.getDan1PostsUrl(1))
                if (response.isSuccessful) {
                    val posts = response.body()?.mapIndexed { index, post ->
                        post.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = index,
                            isFavored = action.isFavoredQuery()
                        )
                    } ?: listOf()
                    NetResult.Success(posts)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun refreshMoe(action: ActionPost): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.moeApi.getPosts(action.getMoePostsUrl(1))
                if (response.isSuccessful) {
                    val posts = response.body()?.mapIndexed { index, post ->
                        post.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = index,
                            isFavored = action.isFavoredQuery()
                        )
                    } ?: listOf()
                    NetResult.Success(posts)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun refreshGel(action: ActionPost): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.gelApi.getPosts(action.getGelPostsUrl(0))
                if (response.isSuccessful) {
                    val posts = response.body()?.posts?.mapIndexed { index, post ->
                        post.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = index
                        )
                    } ?: listOf()
                    NetResult.Success(posts)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun refreshSankaku(action: ActionPost): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.sankakuApi.getPosts(action.getSankakuPostsUrl())
                val netResult = if (response.isSuccessful) {
                    val data = response.body()
                    val next = data?.meta?.next
                    val posts = data?.posts?.mapIndexed { index, post ->
                        post.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = index,
                            isFavored = action.isFavoredQuery()
                        )
                    } ?: listOf()
                    NextManager.create(Next(booruUid = action.booru.uid, query = action.query, next = next))
                    NetResult.Success(posts)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
                netResult
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun refreshShimmie(action: ActionPost): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.shimmieApi.getPosts(action.getShimmiePostsUrl(1))
                if (response.isSuccessful) {
                    val posts = response.body()?.posts?.mapIndexed { index, post ->
                        post.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = index
                        )
                    } ?: listOf()
                    NetResult.Success(posts)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }
}