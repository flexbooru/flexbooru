package onlymash.flexbooru.repository.popular

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.getDanbooruPopularUrl
import onlymash.flexbooru.model.Popular
import onlymash.flexbooru.model.PostDan
import onlymash.flexbooru.repository.NetworkState
import java.io.IOException
import java.util.concurrent.Executor

class PopularDanDataSource(
    private val danbooruApi: DanbooruApi,
    private val popular: Popular,
    private val retryExecutor: Executor) : PageKeyedDataSource<Int, PostDan>() {

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
                             callback: LoadInitialCallback<Int, PostDan>) {
        val request = danbooruApi.getPosts(getDanbooruPopularUrl(popular))
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)

        // triggered by a refresh, we better execute sync
        try {
            val response = request.execute()
            val data = response.body()
            val items = data?: mutableListOf()
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

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, PostDan>) {

    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, PostDan>) {

    }

}