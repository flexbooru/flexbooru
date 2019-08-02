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

package onlymash.flexbooru.repository.post

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
import onlymash.flexbooru.api.*
import onlymash.flexbooru.api.url.*
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.entity.common.TagBlacklist
import onlymash.flexbooru.entity.post.*
import onlymash.flexbooru.extension.NetResult
import onlymash.flexbooru.repository.Listing
import onlymash.flexbooru.repository.NetworkState
import java.util.concurrent.Executor

//posts repo
class PostRepositoryImpl(
    private val db: FlexbooruDatabase,
    private val danbooruOneApi: DanbooruOneApi,
    private val danbooruApi: DanbooruApi,
    private val moebooruApi: MoebooruApi,
    private val gelbooruApi: GelbooruApi,
    private val sankakuApi: SankakuApi,
    private val ioExecutor: Executor) : PostRepository {

    private var danOneBoundaryCallback: PostDanOneBoundaryCallback? = null
    private var danBoundaryCallback: PostDanBoundaryCallback? = null
    private var moeBoundaryCallback: PostMoeBoundaryCallback? = null
    private var gelBoundaryCallback: PostGelBoundaryCallback? = null
    private var sankakuBoundaryCallback: PostSankakuBoundaryCallback? = null

    private fun insertDanbooruOneResultIntoDb(
        search: Search,
        body: MutableList<PostDanOne>?,
        tagBlacklists: MutableList<TagBlacklist>
    ) {
        body?.let { postsDanOne ->
            val start = db.postDanOneDao().getNextIndex(host = search.host, keyword = search.keyword)
            val posts = postsDanOne.mapIndexed { index, post ->
                post.scheme = search.scheme
                post.host = search.host
                post.keyword = search.keyword
                post.indexInResponse = start + index
                post
            }
            val items = mutableListOf<PostDanOne>()
            posts.forEach { post ->
                val index = tagBlacklists.indexOfFirst {
                    post.tags.contains(it.tag)
                }
                if (index == -1) {
                    items.add(post)
                }
            }
            db.postDanOneDao().insert(items)
        }
    }

    private fun insertDanbooruResultIntoDb(
        search: Search,
        body: MutableList<PostDan>?,
        tagBlacklists: MutableList<TagBlacklist>
    ) {
        body?.let { postsDan ->
            val start = db.postDanDao().getNextIndex(host = search.host, keyword = search.keyword)
            val posts = postsDan.mapIndexed { index, post ->
                post.scheme = search.scheme
                post.host = search.host
                post.keyword = search.keyword
                post.indexInResponse = start + index
                post
            }
            val items = mutableListOf<PostDan>()
            posts.forEach { postDan ->
                val index = tagBlacklists.indexOfFirst {
                    postDan.tag_string.contains(it.tag)
                }
                if (index == -1 && !postDan.preview_file_url.isNullOrBlank()) {
                    items.add(postDan)
                }
            }
            db.postDanDao().insert(items)
        }
    }

    private fun insertMoebooruResultIntoDb(
        search: Search,
        body: MutableList<PostMoe>?
    ) {
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

    private fun insertGelbooruResultIntoDb(
        search: Search,
        body: MutableList<PostGel>?,
        tagBlacklists: MutableList<TagBlacklist>
    ) {
        body?.let { data ->
            val start = db.postGelDao().getNextIndex(host = search.host, keyword = search.keyword)
            val posts = data.mapIndexed { index, post ->
                post.scheme = search.scheme
                post.host = search.host
                post.keyword = search.keyword
                post.indexInResponse = start + index
                post
            }
            val items = mutableListOf<PostGel>()
            posts.forEach { post ->
                val index = tagBlacklists.indexOfFirst {
                    post.tags.contains(it.tag)
                }
                if (index == -1) {
                    items.add(post)
                }
            }
            db.postGelDao().insert(items)
        }
    }

    private fun insertSankakuResultIntoDb(
        search: Search,
        body: MutableList<PostSankaku>?,
        tagBlacklists: MutableList<TagBlacklist>
    ) {
        body?.let { data ->
            val start = db.postSankakuDao().getNextIndex(host = search.host, keyword = search.keyword)
            val posts = data.mapIndexed { index, post ->
                post.scheme = search.scheme
                post.host = search.host
                post.keyword = search.keyword
                post.indexInResponse = start + index
                post
            }
            val blackTags = tagBlacklists.map { it.tag }
            val items = mutableListOf<PostSankaku>()
            posts.forEach { postSankaku ->
                if (postSankaku.tags.map { it.name }.intersect(blackTags).isEmpty()) {
                    items.add(postSankaku)
                }
            }
            db.postSankakuDao().insert(items)
        }
    }

    @MainThread
    override fun getDanOnePosts(
        scope: CoroutineScope,
        search: Search,
        tagBlacklists: MutableList<TagBlacklist>
    ): Listing<PostDanOne> {
        danOneBoundaryCallback = PostDanOneBoundaryCallback(
            scope = scope,
            danbooruOneApi = danbooruOneApi,
            handleResponse = this::insertDanbooruOneResultIntoDb,
            ioExecutor = ioExecutor,
            search = search,
            tagBlacklists = tagBlacklists
        )
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refreshDanbooruOne(scope, search, tagBlacklists)
        }
        val livePagedList = db.postDanOneDao()
            .getPosts(search.host, search.keyword)
            .toLiveData(
                config = Config(
                    pageSize = search.limit,
                    enablePlaceholders = true,
                    maxSize = 150
                ),
                boundaryCallback = danOneBoundaryCallback
            )
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
    override fun getDanPosts(
        scope: CoroutineScope,
        search: Search,
        tagBlacklists: MutableList<TagBlacklist>
    ): Listing<PostDan> {
        danBoundaryCallback = PostDanBoundaryCallback(
            scope = scope,
            danbooruApi = danbooruApi,
            handleResponse = this::insertDanbooruResultIntoDb,
            ioExecutor = ioExecutor,
            search = search,
            tagBlacklists = tagBlacklists
        )
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refreshDanbooru(scope, search, tagBlacklists)
        }
        val livePagedList = db.postDanDao()
            .getPosts(search.host, search.keyword)
            .toLiveData(
                config = Config(
                    pageSize = search.limit,
                    enablePlaceholders = true,
                    maxSize = 150
                ),
                boundaryCallback = danBoundaryCallback
            )
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
    override fun getMoePosts(
        scope: CoroutineScope,
        search: Search,
        tagBlacklists: MutableList<TagBlacklist>
    ): Listing<PostMoe> {
        moeBoundaryCallback = PostMoeBoundaryCallback(
            scope = scope,
            moebooruApi = moebooruApi,
            handleResponse = this::insertMoebooruResultIntoDb,
            ioExecutor = ioExecutor,
            search = search,
            tagBlacklists = tagBlacklists
        )
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refreshMoebooru(scope, search, tagBlacklists)
        }
        val livePagedList = db.postMoeDao()
            .getPosts(host = search.host, keyword = search.keyword)
            .toLiveData(
                config = Config(
                    pageSize = search.limit,
                    enablePlaceholders = true,
                    maxSize = 150
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
    override fun getGelPosts(
        scope: CoroutineScope,
        search: Search,
        tagBlacklists: MutableList<TagBlacklist>
    ): Listing<PostGel> {
        gelBoundaryCallback = PostGelBoundaryCallback(
            scope = scope,
            gelbooruApi = gelbooruApi,
            handleResponse = this::insertGelbooruResultIntoDb,
            ioExecutor = ioExecutor,
            search = search,
            tagBlacklists = tagBlacklists
        )
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refreshGelbooru(scope, search, tagBlacklists)
        }
        val livePagedList = db.postGelDao()
            .getPosts(host = search.host, keyword = search.keyword)
            .toLiveData(
                config = Config(
                    pageSize = search.limit,
                    enablePlaceholders = true,
                    maxSize = 150
                ),
                boundaryCallback = gelBoundaryCallback
            )
        return Listing(
            pagedList = livePagedList,
            networkState = gelBoundaryCallback!!.networkState,
            retry = {
                gelBoundaryCallback!!.helper.retryAllFailed()
            },
            refresh = {
                refreshTrigger.value = null
            },
            refreshState = refreshState
        )
    }

    @MainThread
    private fun refreshDanbooruOne(
        scope: CoroutineScope,
        search: Search,
        tagBlacklists: MutableList<TagBlacklist>
    ): LiveData<NetworkState> {
        danOneBoundaryCallback?.lastResponseSize = search.limit
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val response = danbooruOneApi.getPosts(DanOneUrlHelper.getPostUrl(search, 1))
                    db.runInTransaction {
                        db.postDanOneDao().deletePosts(host = search.host, keyword = search.keyword)
                        insertDanbooruOneResultIntoDb(search, response.body(), tagBlacklists)
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
    private fun refreshDanbooru(
        scope: CoroutineScope,
        search: Search,
        tagBlacklists: MutableList<TagBlacklist>
    ): LiveData<NetworkState> {
        danBoundaryCallback?.lastResponseSize = search.limit
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val response = danbooruApi.getPosts(DanUrlHelper.getPostUrl(search, 1))
                    db.runInTransaction {
                        db.postDanDao().deletePosts(host = search.host, keyword = search.keyword)
                        insertDanbooruResultIntoDb(search, response.body(), tagBlacklists)
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
    private fun refreshMoebooru(
        scope: CoroutineScope,
        search: Search,
        tagBlacklists: MutableList<TagBlacklist>
    ): LiveData<NetworkState> {
        moeBoundaryCallback?.lastResponseSize = search.limit
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        var tags = ""
        tagBlacklists.forEach { tagBlacklist ->
            tags = "$tags -${tagBlacklist.tag}"
        }
        tags = "${search.keyword}$tags".trim()
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val response = moebooruApi.getPosts(MoeUrlHelper.getPostUrl(search, 1, tags))
                    db.runInTransaction {
                        db.postMoeDao().deletePosts(host = search.host, keyword = search.keyword)
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

    @MainThread
    private fun refreshGelbooru(
        scope: CoroutineScope,
        search: Search,
        tagBlacklists: MutableList<TagBlacklist>
    ): LiveData<NetworkState> {
        gelBoundaryCallback?.lastResponseSize = search.limit
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val response = gelbooruApi.getPosts(GelUrlHelper.getPostUrl(search, 0))
                    db.runInTransaction {
                        db.postGelDao().deletePosts(search.host, search.keyword)
                        insertGelbooruResultIntoDb(search, response.body()?.posts, tagBlacklists)
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
    override fun getSankakuPosts(
        scope: CoroutineScope,
        search: Search,
        tagBlacklists: MutableList<TagBlacklist>
    ): Listing<PostSankaku> {
        sankakuBoundaryCallback = PostSankakuBoundaryCallback(
            scope = scope,
            sankakuApi = sankakuApi,
            handleResponse = this::insertSankakuResultIntoDb,
            ioExecutor = ioExecutor,
            search = search,
            tagBlacklists = tagBlacklists
        )
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refreshSankaku(scope, search, tagBlacklists)
        }
        val livePagedList = db.postSankakuDao()
            .getPosts(host = search.host, keyword = search.keyword)
            .toLiveData(
                config = Config(
                    pageSize = search.limit,
                    enablePlaceholders = true,
                    maxSize = 150
                ),
                boundaryCallback = sankakuBoundaryCallback
            )
        return Listing(
            pagedList = livePagedList,
            networkState = sankakuBoundaryCallback!!.networkState,
            retry = {
                sankakuBoundaryCallback!!.helper.retryAllFailed()
            },
            refresh = {
                refreshTrigger.value = null
            },
            refreshState = refreshState
        )
    }

    @MainThread
    private fun refreshSankaku(
        scope: CoroutineScope,
        search: Search,
        tagBlacklists: MutableList<TagBlacklist>
    ): LiveData<NetworkState> {
        sankakuBoundaryCallback?.lastResponseSize = search.limit
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val response = sankakuApi.getPosts(SankakuUrlHelper.getPostUrl(search, 1))
                    db.runInTransaction {
                        db.postSankakuDao().deletePosts(search.host, search.keyword)
                        insertSankakuResultIntoDb(search, response.body(), tagBlacklists)
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
}