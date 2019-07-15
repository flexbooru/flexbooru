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

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.toLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.DanbooruOneApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.api.SankakuApi
import onlymash.flexbooru.api.url.DanOneUrlHelper
import onlymash.flexbooru.api.url.DanUrlHelper
import onlymash.flexbooru.api.url.MoeUrlHelper
import onlymash.flexbooru.api.url.SankakuUrlHelper
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.entity.post.*
import onlymash.flexbooru.extension.NetResult
import onlymash.flexbooru.repository.Listing
import onlymash.flexbooru.repository.NetworkState
import java.util.concurrent.Executor

private const val POPULAR_QUERY = "popular"

//popular posts data source
class PopularRepositoryImpl(
    private val danbooruApi: DanbooruApi,
    private val danbooruOneApi: DanbooruOneApi,
    private val moebooruApi: MoebooruApi,
    private val sankakuApi: SankakuApi,
    private val db: FlexbooruDatabase,
    private val networkExecutor: Executor) : PopularRepository {

    private var danBoundaryCallback: PopularDanBoundaryCallback? = null
    private var danOneBoundaryCallback: PopularDanOneBoundaryCallback? = null
    private var moeBoundaryCallback: PopularMoeBoundaryCallback? = null
    private var sankakuBoundaryCallback: PopularSankakuBoundaryCallback? = null

    private fun insertDanbooruOneResultIntoDb(
        search: SearchPopular,
        body: MutableList<PostDanOne>?
    ) {
        body?.let { postsDanOne ->
            val start = db.postDanOneDao().getNextIndex(host = search.host, keyword = POPULAR_QUERY)
            val posts = postsDanOne.mapIndexed { index, post ->
                post.scheme = search.scheme
                post.host = search.host
                post.keyword = POPULAR_QUERY
                post.indexInResponse = start + index
                post
            }
            db.postDanOneDao().insert(posts)
        }
    }

    private fun insertDanbooruResultIntoDb(
        search: SearchPopular,
        body: MutableList<PostDan>?
    ) {
        body?.let { postsDan ->
            val start = db.postDanDao().getNextIndex(host = search.host, keyword = POPULAR_QUERY)
            val posts = postsDan.mapIndexed { index, post ->
                post.scheme = search.scheme
                post.host = search.host
                post.keyword = POPULAR_QUERY
                post.indexInResponse = start + index
                post
            }
            db.postDanDao().insert(posts)
        }
    }

    private fun insertMoebooruResultIntoDb(
        search: SearchPopular,
        body: MutableList<PostMoe>?
    ) {
        body?.let { posts ->
            val start = db.postMoeDao().getNextIndex(host = search.host, keyword = POPULAR_QUERY)
            val items = posts.mapIndexed { index, post ->
                post.scheme = search.scheme
                post.host = search.host
                post.keyword = POPULAR_QUERY
                post.indexInResponse = start + index
                post
            }
            db.postMoeDao().insert(items)
        }
    }

    private fun insertSankakuResultIntoDb(
        search: SearchPopular,
        body: MutableList<PostSankaku>?
    ) {
        body?.let { data ->
            val start = db.postSankakuDao().getNextIndex(host = search.host, keyword = POPULAR_QUERY)
            val posts = data.mapIndexed { index, post ->
                post.scheme = search.scheme
                post.host = search.host
                post.keyword = POPULAR_QUERY
                post.indexInResponse = start + index
                post
            }
            db.postSankakuDao().insert(posts)
        }
    }

    @MainThread
    private fun refreshDanbooru(
        scope: CoroutineScope,
        search: SearchPopular
    ): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val response = danbooruApi.getPosts(DanUrlHelper.getPopularUrl(search))
                    db.runInTransaction {
                        db.postDanDao().deletePosts(host = search.host, keyword = POPULAR_QUERY)
                        insertDanbooruResultIntoDb(search, response.body())
                    }
                    NetResult.Success(NetworkState.LOADED)
                } catch (e: Exception) {
                    NetResult.Success(NetworkState.error(e.message))
                }
            }
            when (val data = result.data) {
                NetworkState.LOADED -> networkState.postValue(data)
                else -> networkState.value = data
            }
        }
        return networkState
    }

    @MainThread
    override fun getDanPopular(
        scope: CoroutineScope,
        search: SearchPopular): Listing<PostDan> {
        danBoundaryCallback = PopularDanBoundaryCallback(
            scope = scope,
            danbooruApi = danbooruApi,
            handleResponse = this::insertDanbooruResultIntoDb,
            ioExecutor = networkExecutor,
            search = search
        )
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refreshDanbooru(scope, search)
        }
        val livePagedList = db.postDanDao()
            .getPosts(search.host, POPULAR_QUERY)
            .toLiveData(
                config = Config(
                    pageSize = 20,
                    enablePlaceholders = true
                ),
                boundaryCallback = danBoundaryCallback
            )
        return Listing(
            pagedList = livePagedList,
            networkState = danBoundaryCallback!!.networkState,
            retry = { danBoundaryCallback!!.helper.retryAllFailed() },
            refresh = { refreshTrigger.value = null },
            refreshState = refreshState
        )
    }

    @MainThread
    private fun refreshDanbooruOne(
        scope: CoroutineScope,
        search: SearchPopular
    ): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val response = danbooruOneApi.getPosts(DanOneUrlHelper.getPopularUrl(search))
                    db.runInTransaction {
                        db.postDanOneDao().deletePosts(host = search.host, keyword = POPULAR_QUERY)
                        insertDanbooruOneResultIntoDb(search, response.body())
                    }
                    NetResult.Success(NetworkState.LOADED)
                } catch (e: Exception) {
                    NetResult.Success(NetworkState.error(e.message))
                }
            }
            when (val data = result.data) {
                NetworkState.LOADED -> networkState.postValue(data)
                else -> networkState.value = data
            }
        }
        return networkState
    }

    @MainThread
    override fun getDanOnePopular(
        scope: CoroutineScope,
        search: SearchPopular): Listing<PostDanOne> {
        danOneBoundaryCallback = PopularDanOneBoundaryCallback(
            scope = scope,
            danbooruOneApi = danbooruOneApi,
            handleResponse = this::insertDanbooruOneResultIntoDb,
            ioExecutor = networkExecutor,
            search = search
        )
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refreshDanbooruOne(scope, search)
        }
        val livePagedList = db.postDanOneDao()
            .getPosts(search.host, POPULAR_QUERY)
            .toLiveData(
                config = Config(
                    pageSize = 20,
                    enablePlaceholders = true
                ),
                boundaryCallback = danOneBoundaryCallback
            )
        return Listing(
            pagedList = livePagedList,
            networkState = danOneBoundaryCallback!!.networkState,
            retry = { danOneBoundaryCallback!!.helper.retryAllFailed() },
            refresh = { refreshTrigger.value = null },
            refreshState = refreshState
        )
    }

    @MainThread
    private fun refreshMoebooru(
        scope: CoroutineScope,
        search: SearchPopular
    ): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val response = moebooruApi.getPosts(MoeUrlHelper.getPopularUrl(search))
                    db.runInTransaction {
                        db.postMoeDao().deletePosts(host = search.host, keyword = POPULAR_QUERY)
                        insertMoebooruResultIntoDb(search, response.body())
                    }
                    NetResult.Success(NetworkState.LOADED)
                } catch (e: Exception) {
                    NetResult.Success(NetworkState.error(e.message))
                }
            }
            when (val data = result.data) {
                NetworkState.LOADED -> networkState.postValue(data)
                else -> networkState.value = data
            }
        }
        return networkState
    }

    override fun getMoePopular(scope: CoroutineScope, search: SearchPopular): Listing<PostMoe> {
        moeBoundaryCallback = PopularMoeBoundaryCallback(
            scope = scope,
            moebooruApi = moebooruApi,
            handleResponse = this::insertMoebooruResultIntoDb,
            ioExecutor = networkExecutor,
            search = search
        )
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refreshMoebooru(scope, search)
        }
        val livePagedList = db.postMoeDao()
            .getPosts(search.host, POPULAR_QUERY)
            .toLiveData(
                config = Config(
                    pageSize = 40,
                    enablePlaceholders = true
                ),
                boundaryCallback = moeBoundaryCallback
            )
        return Listing(
            pagedList = livePagedList,
            networkState = moeBoundaryCallback!!.networkState,
            retry = { moeBoundaryCallback!!.helper.retryAllFailed() },
            refresh = { refreshTrigger.value = null },
            refreshState = refreshState
        )
    }

    @MainThread
    private fun refreshSankaku(
        scope: CoroutineScope,
        search: SearchPopular
    ): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val response = sankakuApi.getPosts(SankakuUrlHelper.getPopularUrl(search, 1))
                    db.runInTransaction {
                        db.postSankakuDao().deletePosts(host = search.host, keyword = POPULAR_QUERY)
                        insertSankakuResultIntoDb(search, response.body())
                    }
                    NetResult.Success(NetworkState.LOADED)
                } catch (e: Exception) {
                    NetResult.Success(NetworkState.error(e.message))
                }
            }
            when (val data = result.data) {
                NetworkState.LOADED -> networkState.postValue(data)
                else -> networkState.value = data
            }
        }
        return networkState
    }

    override fun getSankakuPopular(
        scope: CoroutineScope,
        search: SearchPopular
    ): Listing<PostSankaku> {
        sankakuBoundaryCallback = PopularSankakuBoundaryCallback(
            scope = scope,
            sankakuApi = sankakuApi,
            handleResponse = this::insertSankakuResultIntoDb,
            ioExecutor = networkExecutor,
            search = search
        )
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refreshSankaku(scope, search)
        }
        val livePagedList = db.postSankakuDao()
            .getPosts(search.host, POPULAR_QUERY)
            .toLiveData(
                config = Config(
                    pageSize = search.limit,
                    enablePlaceholders = true
                ),
                boundaryCallback = sankakuBoundaryCallback
            )
        return Listing(
            pagedList = livePagedList,
            networkState = sankakuBoundaryCallback!!.networkState,
            retry = { sankakuBoundaryCallback!!.helper.retryAllFailed() },
            refresh = { refreshTrigger.value = null },
            refreshState = refreshState
        )
    }
}