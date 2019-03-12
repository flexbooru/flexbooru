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

package onlymash.flexbooru.repository.post

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.toLiveData
import onlymash.flexbooru.api.url.MoeUrlHelper
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.DanbooruOneApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.api.url.DanOneUrlHelper
import onlymash.flexbooru.api.url.DanUrlHelper
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.entity.post.PostDan
import onlymash.flexbooru.entity.post.PostDanOne
import onlymash.flexbooru.entity.post.PostMoe
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.repository.Listing
import onlymash.flexbooru.repository.NetworkState
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

//posts repo
class PostData(
    private val db: FlexbooruDatabase,
    private val danbooruOneApi: DanbooruOneApi,
    private val danbooruApi: DanbooruApi,
    private val moebooruApi: MoebooruApi,
    private val ioExecutor: Executor) : PostRepository {

    private var danOneBoundaryCallback: PostDanOneBoundaryCallback? = null
    private var danBoundaryCallback: PostDanBoundaryCallback? = null
    private var moeBoundaryCallback: PostMoeBoundaryCallback? = null

    private fun insertDanbooruOneResultIntoDb(search: Search, body: MutableList<PostDanOne>?) {
        body?.let { postsDanOne ->
            val start = db.postDanOneDao().getNextIndex(host = search.host, keyword = search.keyword)
            val items = postsDanOne.mapIndexed { index, post ->
                post.scheme = search.scheme
                post.host = search.host
                post.keyword = search.keyword
                post.indexInResponse = start + index
                post
            }
            db.postDanOneDao().insert(items)
        }
    }

    private fun insertDanbooruResultIntoDb(search: Search, body: MutableList<PostDan>?) {
        body?.let { postsDan ->
            val posts = mutableListOf<PostDan>()
            postsDan.forEach { postDan ->
                if (!postDan.preview_file_url.isNullOrBlank()) {
                    posts.add(postDan)
                }
            }
            val start = db.postDanDao().getNextIndex(host = search.host, keyword = search.keyword)
            val items = posts.mapIndexed { index, post ->
                post.scheme = search.scheme
                post.host = search.host
                post.keyword = search.keyword
                post.indexInResponse = start + index
                post
            }
            db.postDanDao().insert(items)
        }
    }

    private fun insertMoebooruResultIntoDb(search: Search, body: MutableList<PostMoe>?) {
        body?.let { posts ->
            val start = db.postMoeDao().getNextIndex(host = search.host, keyword = search.keyword)
            val items = posts.mapIndexed { index, post ->
                post.scheme = search.scheme
                post.host = search.host
                post.keyword = search.keyword
                post.indexInResponse = start + index
                post
            }
            db.postMoeDao().insert(items)
        }
    }

    @MainThread
    override fun getDanOnePosts(search: Search): Listing<PostDanOne> {
        danOneBoundaryCallback = PostDanOneBoundaryCallback(
            danbooruOneApi = danbooruOneApi,
            handleResponse = this::insertDanbooruOneResultIntoDb,
            ioExecutor = ioExecutor,
            search = search
        )
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refreshDanbooruOne(search)
        }
        val livePagedList = db.postDanOneDao()
            .getPosts(search.host, search.keyword)
            .toLiveData(
                config = Config(
                    pageSize = search.limit,
                    enablePlaceholders = true,
                    maxSize = 100
                ),
                boundaryCallback = danOneBoundaryCallback)
        return Listing(
            pagedList = livePagedList,
            networkState = danOneBoundaryCallback!!.networkState,
            retry = {
                danOneBoundaryCallback!!.helper.retryAllFailed()
            },
            refresh = {
                refreshTrigger.value = null
            },
            refreshState = refreshState
        )
    }

    @MainThread
    override fun getDanPosts(search: Search): Listing<PostDan> {
        danBoundaryCallback = PostDanBoundaryCallback(
            danbooruApi = danbooruApi,
            handleResponse = this::insertDanbooruResultIntoDb,
            ioExecutor = ioExecutor,
            search = search
        )
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refreshDanbooru(search)
        }
        val livePagedList = db.postDanDao()
            .getPosts(search.host, search.keyword)
            .toLiveData(
                config = Config(
                    pageSize = search.limit,
                    enablePlaceholders = true,
                    maxSize = 100
                ),
                boundaryCallback = danBoundaryCallback)
        return Listing(
            pagedList = livePagedList,
            networkState = danBoundaryCallback!!.networkState,
            retry = {
                danBoundaryCallback!!.helper.retryAllFailed()
            },
            refresh = {
                refreshTrigger.value = null
            },
            refreshState = refreshState
        )
    }

    @MainThread
    override fun getMoePosts(search: Search): Listing<PostMoe> {
        moeBoundaryCallback = PostMoeBoundaryCallback(
            moebooruApi = moebooruApi,
            handleResponse = this::insertMoebooruResultIntoDb,
            ioExecutor = ioExecutor,
            search = search
        )
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refreshMoebooru(search)
        }
        val livePagedList = db.postMoeDao()
            .getPosts(host = search.host, keyword = search.keyword)
            .toLiveData(
                config = Config(
                    pageSize = search.limit,
                    enablePlaceholders = true,
                    maxSize = 100
                ),
                boundaryCallback = moeBoundaryCallback
            )
        return Listing(
            pagedList = livePagedList,
            networkState = moeBoundaryCallback!!.networkState,
            retry = {
                moeBoundaryCallback!!.helper.retryAllFailed()
            },
            refresh = {
                refreshTrigger.value = null
            },
            refreshState = refreshState
        )
    }

    @MainThread
    private fun refreshDanbooruOne(search: Search): LiveData<NetworkState> {
        danOneBoundaryCallback?.lastResponseSize = search.limit
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        danbooruOneApi.getPosts(DanOneUrlHelper.getPostUrl(search, 1)).enqueue(
            object : Callback<MutableList<PostDanOne>> {
                override fun onFailure(call: Call<MutableList<PostDanOne>>, t: Throwable) {
                    networkState.value = NetworkState.error(t.message)
                }
                override fun onResponse(call: Call<MutableList<PostDanOne>>,
                                        response: Response<MutableList<PostDanOne>>) {
                    ioExecutor.execute {
                        db.runInTransaction {
                            val posts = response.body()
                            if (posts.isNullOrEmpty()) {
                                db.postDanOneDao().deletePosts(host = search.host, keyword = search.keyword)
                                return@runInTransaction
                            }
                            val first = db.postDanOneDao()
                                .getFirstPostRaw(host = search.host, keyword = search.keyword)
                            if (first != null && first.id >= posts[0].id) {
                                return@runInTransaction
                            }
                            db.postDanDao().deletePosts(host = search.host, keyword = search.keyword)
                            insertDanbooruOneResultIntoDb(search, posts)
                        }
                    }
                    networkState.postValue(NetworkState.LOADED)
                }
            }
        )
        return networkState
    }

    @MainThread
    private fun refreshDanbooru(search: Search): LiveData<NetworkState> {
        danBoundaryCallback?.lastResponseSize = search.limit
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        danbooruApi.getPosts(DanUrlHelper.getPostUrl(search, 1)).enqueue(
            object : Callback<MutableList<PostDan>> {
                override fun onFailure(call: Call<MutableList<PostDan>>, t: Throwable) {
                    networkState.value = NetworkState.error(t.message)
                }

                override fun onResponse(call: Call<MutableList<PostDan>>, response: Response<MutableList<PostDan>>) {
                    ioExecutor.execute {
                        db.runInTransaction {
                            val posts = response.body()
                            if (posts.isNullOrEmpty()) {
                                db.postDanDao().deletePosts(host = search.host, keyword = search.keyword)
                                return@runInTransaction
                            }
                            val first = db.postDanDao().getFirstPostRaw(host = search.host, keyword = search.keyword)
                            if (first != null && first.id >= posts[0].id) {
                                return@runInTransaction
                            }
                            db.postDanDao().deletePosts(host = search.host, keyword = search.keyword)
                            insertDanbooruResultIntoDb(search, posts)
                        }
                    }
                    networkState.postValue(NetworkState.LOADED)
                }
            }
        )
        return networkState
    }

    @MainThread
    private fun refreshMoebooru(search: Search): LiveData<NetworkState> {
        moeBoundaryCallback?.lastResponseSize = search.limit
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        moebooruApi.getPosts(MoeUrlHelper.getPostUrl(search, 1)).enqueue(
            object : Callback<MutableList<PostMoe>> {
                override fun onFailure(call: Call<MutableList<PostMoe>>, t: Throwable) {
                    networkState.value = NetworkState.error(t.message)
                }

                override fun onResponse(call: Call<MutableList<PostMoe>>, response: Response<MutableList<PostMoe>>) {
                    ioExecutor.execute {
                        db.runInTransaction {
                            val posts = response.body()
                            if (posts.isNullOrEmpty()) {
                                db.postMoeDao().deletePosts(host = search.host, keyword = search.keyword)
                                return@runInTransaction
                            }
                            val first = db.postMoeDao().getFirstPostRaw(host = search.host, keyword = search.keyword)
                            if (first != null && first.id >= posts[0].id) {
                                return@runInTransaction
                            }
                            db.postMoeDao().deletePosts(search.host, search.keyword)
                            insertMoebooruResultIntoDb(search, posts)
                        }
                    }
                    networkState.postValue(NetworkState.LOADED)
                }
            }
        )
        return networkState
    }
}