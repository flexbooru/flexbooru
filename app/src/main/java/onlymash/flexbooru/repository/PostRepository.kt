package onlymash.flexbooru.repository

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.toLiveData
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.api.getDanbooruUrl
import onlymash.flexbooru.api.getMoebooruUrl
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.model.PostDan
import onlymash.flexbooru.model.PostMoe
import onlymash.flexbooru.model.Search
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

class PostRepository(
    private val db: FlexbooruDatabase,
    private val danbooruApi: DanbooruApi,
    private val moebooruApi: MoebooruApi,
    private val ioExecutor: Executor) : Repository {

    private fun insertDanbooruResultIntoDb(search: Search, body: MutableList<PostDan>?) {
        body?.let { posts ->
            val start = db.postDanDao().getNextIndex(host = search.host, keyword = search.tags)
            val items = posts.mapIndexed { index, post ->
                post.host = search.host
                post.keyword = search.tags
                post.indexInResponse = start + index
                post
            }
            db.postDanDao().insert(items)
        }
    }

    private fun insertMoebooruResultIntoDb(search: Search, body: MutableList<PostMoe>?) {
        body?.let { posts ->
            val start = db.postMoeDao().getNextIndex(host = search.host, keyword = search.tags)
            val items = posts.mapIndexed { index, post ->
                post.host = search.host
                post.keyword = search.tags
                post.indexInResponse = start + index
                post
            }
            db.postMoeDao().insert(items)
        }
    }

    @MainThread
    override fun getDanbooruPosts(search: Search): Listing<PostDan> {
        val boundaryCallback = PostDanBoundaryCallback(
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
            .getPosts(search.host, search.tags)
            .toLiveData(
                pageSize = search.limit,
                boundaryCallback = boundaryCallback
            )
        return Listing(
            pagedList = livePagedList,
            networkState = boundaryCallback.networkState,
            retry = {
                boundaryCallback.helper.retryAllFailed()
            },
            refresh = {
                refreshTrigger.value = null
            },
            refreshState = refreshState
        )
    }

    @MainThread
    override fun getMoebooruPosts(search: Search): Listing<PostMoe> {
        val boundaryCallback = PostMoeBoundaryCallback(
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
            .getPosts(host = search.host, keyword = search.tags)
            .toLiveData(
                pageSize = search.limit,
                boundaryCallback = boundaryCallback
            )
        return Listing(
            pagedList = livePagedList,
            networkState = boundaryCallback.networkState,
            retry = {
                boundaryCallback.helper.retryAllFailed()
            },
            refresh = {
                refreshTrigger.value = null
            },
            refreshState = refreshState
        )
    }

    @MainThread
    private fun refreshDanbooru(search: Search): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        danbooruApi.getPosts(getDanbooruUrl(search, 1)).enqueue(
            object : Callback<MutableList<PostDan>> {
                override fun onFailure(call: Call<MutableList<PostDan>>, t: Throwable) {
                    networkState.value = NetworkState.error(t.message)
                }

                override fun onResponse(call: Call<MutableList<PostDan>>, response: Response<MutableList<PostDan>>) {
                    ioExecutor.execute {
                        db.runInTransaction {
                            db.postDanDao().deletePosts(host = search.host, keyword = search.tags)
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
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        moebooruApi.getPosts(getMoebooruUrl(search, 1)).enqueue(
            object : Callback<MutableList<PostMoe>> {
                override fun onFailure(call: Call<MutableList<PostMoe>>, t: Throwable) {
                    networkState.value = NetworkState.error(t.message)
                }

                override fun onResponse(call: Call<MutableList<PostMoe>>, response: Response<MutableList<PostMoe>>) {
                    ioExecutor.execute {
                        db.runInTransaction {
                            db.postMoeDao().deletePosts(search.host, search.tags)
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