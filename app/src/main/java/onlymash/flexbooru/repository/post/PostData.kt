package onlymash.flexbooru.repository.post

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.toLiveData
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.api.getDanUrl
import onlymash.flexbooru.api.getMoeUrl
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.model.PostDan
import onlymash.flexbooru.model.PostMoe
import onlymash.flexbooru.model.Search
import onlymash.flexbooru.repository.Listing
import onlymash.flexbooru.repository.NetworkState
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

class PostData(
    private val db: FlexbooruDatabase,
    private val danbooruApi: DanbooruApi,
    private val moebooruApi: MoebooruApi,
    private val ioExecutor: Executor) : PostRepository {

    private var danBoundaryCallback: PostDanBoundaryCallback? = null
    private var moeBoundaryCallback: PostMoeBoundaryCallback? = null

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
                post.host = search.host
                post.keyword = search.keyword
                post.indexInResponse = start + index
                post
            }
            db.postMoeDao().insert(items)
        }
    }

    @MainThread
    override fun getDanbooruPosts(search: Search): Listing<PostDan> {
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
    override fun getMoebooruPosts(search: Search): Listing<PostMoe> {
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
    private fun refreshDanbooru(search: Search): LiveData<NetworkState> {
        danBoundaryCallback?.lastResponseSize = search.limit
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        danbooruApi.getPosts(getDanUrl(search, 1)).enqueue(
            object : Callback<MutableList<PostDan>> {
                override fun onFailure(call: Call<MutableList<PostDan>>, t: Throwable) {
                    networkState.value = NetworkState.error(t.message)
                }

                override fun onResponse(call: Call<MutableList<PostDan>>, response: Response<MutableList<PostDan>>) {
                    ioExecutor.execute {
                        db.runInTransaction {
                            db.postDanDao().deletePosts(host = search.host, keyword = search.keyword)
                            insertDanbooruResultIntoDb(search, response.body())
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
        moebooruApi.getPosts(getMoeUrl(search, 1)).enqueue(
            object : Callback<MutableList<PostMoe>> {
                override fun onFailure(call: Call<MutableList<PostMoe>>, t: Throwable) {
                    networkState.value = NetworkState.error(t.message)
                }

                override fun onResponse(call: Call<MutableList<PostMoe>>, response: Response<MutableList<PostMoe>>) {
                    ioExecutor.execute {
                        db.runInTransaction {
                            db.postMoeDao().deletePosts(search.host, search.keyword)
                            insertMoebooruResultIntoDb(search, response.body())
                        }
                    }
                    networkState.postValue(NetworkState.LOADED)
                }
            }
        )
        return networkState
    }
}