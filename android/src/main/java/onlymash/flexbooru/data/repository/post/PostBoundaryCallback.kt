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

import androidx.paging.PagedList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.app.Values.BOORU_TYPE_SHIMMIE
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.app.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.app.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.app.Values.PAGE_TYPE_POPULAR
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.action.ActionPost
import onlymash.flexbooru.data.api.DanbooruApi
import onlymash.flexbooru.data.database.NextManager
import onlymash.flexbooru.data.model.common.Next
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.data.repository.PagingRequestHelper
import onlymash.flexbooru.extension.NetResult
import onlymash.flexbooru.extension.createStatusLiveData

class PostBoundaryCallback(
    private val action: ActionPost,
    private val booruApis: BooruApis,
    private val scope: CoroutineScope,
    private val handleResponse: (List<Post>) -> Unit
) : PagedList.BoundaryCallback<Post>() {

    private val isFavored = action.isFavoredQuery()

    //PagingRequestHelper
    val helper = PagingRequestHelper()
    //network state
    val networkState = helper.createStatusLiveData()

    //last response posts size
    var lastResponseSize = action.limit

    override fun onZeroItemsLoaded() {
        scope.launch {
            helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) { callback ->
                val page = when (action.booru.type) {
                    BOORU_TYPE_GEL -> 0
                    else -> 1
                }
                createCallback(page, 0, callback)
            }
        }
    }

    override fun onItemAtEndLoaded(itemAtEnd: Post) {
        when {
            action.pageType == PAGE_TYPE_POPULAR &&
                    action.booru.type != BOORU_TYPE_SANKAKU -> return
            action.booru.type == BOORU_TYPE_SANKAKU -> {
                NextManager.getNext(action.booru.uid, action.query)?.next?.let { next ->
                    scope.launch {
                        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) { callback ->
                            createCallback(indexInNext = itemAtEnd.index + 1, callback = callback, next = next)
                        }
                    }
                }
            }
            else -> {
                val indexInNext = itemAtEnd.index + 1
                val limit = action.limit
                if (lastResponseSize == limit) {
                    scope.launch {
                        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) { callback ->
                            val page = when (action.booru.type) {
                                BOORU_TYPE_GEL -> indexInNext/limit
                                else -> indexInNext/limit + 1
                            }
                            createCallback(page, indexInNext, callback)
                        }
                    }
                }
            }
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: Post) {

    }

    private fun createCallback(page: Int = 1, indexInNext: Int, callback: PagingRequestHelper.Callback, next: String = "") {
        scope.launch {
            when (val result = when (action.booru.type) {
                BOORU_TYPE_DAN -> {
                    if (action.booru.host in DanbooruApi.E621_HOSTS) {
                        getPostsDanE621(page, indexInNext)
                    } else {
                        getPostsDan(page, indexInNext)
                    }
                }
                BOORU_TYPE_DAN1 -> getPostsDan1(page, indexInNext)
                BOORU_TYPE_MOE -> getPostsMoe(page, indexInNext)
                BOORU_TYPE_GEL -> getPostsGel(page, indexInNext)
                BOORU_TYPE_SHIMMIE -> getPostsShimmie(page, indexInNext)
                else -> getPostsSankaku(next, indexInNext)
            }) {
                is NetResult.Success -> {
                    callback.recordSuccess()
                    insertItemsIntoDb(result.data)
                }
                is NetResult.Error -> callback.recordFailure(Throwable(result.errorMsg))
            }
        }
    }

    private suspend fun insertItemsIntoDb(posts: List<Post>) {
        withContext(Dispatchers.IO) {
            handleResponse(posts)
        }
    }

    private suspend fun getPostsDan(page: Int, indexInNext: Int): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.danApi.getPosts(
                    action.getDanPostsUrl(page))
                if (response.isSuccessful) {
                    val raw = response.body()?.toMutableList()
                    lastResponseSize = raw?.size ?: 0
                    raw?.removeIf { it.id == -1 }
                    val posts = raw?.mapIndexed { index, postDan ->
                        postDan.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = indexInNext + index
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

    private suspend fun getPostsDanE621(page: Int, indexInNext: Int): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.danApi.getPostsE621(
                    action.getDanPostsUrl(page))
                if (response.isSuccessful) {
                    val raw = response.body()?.posts?.toMutableList()
                    lastResponseSize = raw?.size ?: 0
                    raw?.removeIf { it.id == -1 }
                    val posts = raw?.mapIndexed { index, postDan ->
                        postDan.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            index = indexInNext + index
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

    private suspend fun getPostsDan1(page: Int, indexInNext: Int): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.dan1Api.getPosts(
                    action.getDan1PostsUrl(page))
                if (response.isSuccessful) {
                    val raw = response.body()
                    lastResponseSize = raw?.size ?: 0
                    val posts = raw?.mapIndexed { index, postDan1 ->
                        postDan1.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = indexInNext + index,
                            isFavored = isFavored
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

    private suspend fun getPostsMoe(page: Int, indexInNext: Int): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.moeApi.getPosts(
                    action.getMoePostsUrl(page))
                if (response.isSuccessful) {
                    val raw = response.body()
                    lastResponseSize = raw?.size ?: 0
                    val posts = raw?.mapIndexed { index, postMoe ->
                        postMoe.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = indexInNext + index,
                            isFavored = isFavored
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

    private suspend fun getPostsGel(page: Int, indexInNext: Int): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.gelApi.getPosts(
                    action.getGelPostsUrl(page))
                if (response.isSuccessful) {
                    val raw = response.body()?.posts
                    lastResponseSize = raw?.size ?: 0
                    val posts = raw?.mapIndexed { index, postGel ->
                        postGel.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = indexInNext + index
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

    private suspend fun getPostsSankaku(next: String, indexInNext: Int): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val url = if (indexInNext == 0) action.getSankakuPostsUrl() else action.getSankakuUrlNext(next)
                val auth = action.booru.auth
                val response = if (auth.isNullOrBlank())
                    booruApis.sankakuApi.getPosts(url)
                else
                    booruApis.sankakuApi.getPostsAuth(url, auth)
                if (response.isSuccessful) {
                    val data = response.body()
                    val raw = data?.posts
                    lastResponseSize = raw?.size ?: 0
                    val posts = raw?.mapIndexed { index, postSankaku ->
                        postSankaku.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = indexInNext + index,
                            isFavored = isFavored
                        )
                    } ?: listOf()
                    NextManager.create(Next(booruUid = action.booru.uid, query = action.query, next = data?.meta?.next))
                    NetResult.Success(posts)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun getPostsShimmie(page: Int, indexInNext: Int): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.shimmieApi.getPosts(
                    action.getShimmiePostsUrl(page))
                if (response.isSuccessful) {
                    val raw = response.body()?.posts
                    lastResponseSize = raw?.size ?: 0
                    val posts = raw?.mapIndexed { index, postShimmie ->
                        postShimmie.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = indexInNext + index
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