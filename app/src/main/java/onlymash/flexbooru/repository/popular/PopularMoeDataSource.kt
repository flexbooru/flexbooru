package onlymash.flexbooru.repository.popular

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.api.getMoebooruPopularUrl
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.model.Popular
import onlymash.flexbooru.model.PostMoe
import onlymash.flexbooru.repository.NetworkState
import java.io.IOException
import java.util.concurrent.Executor

class PopularMoeDataSource(
    private val moebooruApi: MoebooruApi,
    private val db: FlexbooruDatabase,
    private val popular: Popular,
    private val retryExecutor: Executor) : PageKeyedDataSource<Int, PostMoe>() {

    // keep a function reference for the retry event
    private var retry: (() -> Any)? = null

    /**
     * There is no sync on the state because paging will always call loadInitial first then wait
     * for it to return some success value before calling loadAfter.
     */
    val networkState = MutableLiveData<NetworkState>()

    val initialLoad = MutableLiveData<NetworkState>()

    fun retryAllFailed() {
        val prevRetry = retry
        retry = null
        prevRetry?.let {
            retryExecutor.execute {
                it.invoke()
            }
        }
    }

    override fun loadInitial(params: LoadInitialParams<Int>,
                             callback: LoadInitialCallback<Int, PostMoe>) {
        val request = moebooruApi.getPosts(getMoebooruPopularUrl(popular))
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)

        val host = popular.host
        val keyword = popular.period

        // triggered by a refresh, we better execute sync
        try {
            val response = request.execute()
            var data = response.body()?: mutableListOf()
            if (popular.safe_mode) {
                val tmp: MutableList<PostMoe> = mutableListOf()
                data.forEach {
                    if (it.rating == "s") {
                        tmp.add(it)
                    }
                }
                data = tmp
            }
            db.postMoeDao().deletePosts(host, keyword)
            val start = db.postMoeDao().getNextIndex(host, keyword)
            val items = data.mapIndexed { index, postMoe ->
                postMoe.host = host
                postMoe.keyword = keyword
                postMoe.indexInResponse = start + index
                postMoe
            }
            db.postMoeDao().insert(items)
            retry = null
            networkState.postValue(NetworkState.LOADED)
            initialLoad.postValue(NetworkState.LOADED)
            callback.onResult(items, 1, 2)
        } catch (ioException: IOException) {
            retry = {
                loadInitial(params, callback)
            }
            val error = NetworkState.error(ioException.message ?: "unknown error")
            networkState.postValue(error)
            initialLoad.postValue(error)
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, PostMoe>) {

    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, PostMoe>) {

    }

}