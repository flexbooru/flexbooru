package onlymash.flexbooru.repository.popular

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import onlymash.flexbooru.api.SankakuApi
import onlymash.flexbooru.api.url.SankakuUrlHelper
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.entity.post.SearchPopular
import onlymash.flexbooru.entity.post.PostSankaku
import onlymash.flexbooru.repository.NetworkState
import java.io.IOException
import java.util.concurrent.Executor

//sankaku popular posts data source
class PopularSankakuDataSource(
    private val sankakuApi: SankakuApi,
    private val db: FlexbooruDatabase,
    private val popular: SearchPopular,
    private val retryExecutor: Executor) : PageKeyedDataSource<Int, PostSankaku>() {

    // keep a function reference for the retry event
    private var retry: (() -> Any)? = null

    /**
     * There is no sync on the state because paging will always call loadInitial first then wait
     * for it to return some success value before calling loadAfter.
     */
    val networkState = MutableLiveData<NetworkState>()

    val initialLoad = MutableLiveData<NetworkState>()

    //retry failed request
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
                             callback: LoadInitialCallback<Int, PostSankaku>) {
        val request = sankakuApi.getPosts(SankakuUrlHelper.getPopularUrl(popular))
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)

        val scheme = popular.scheme
        val host = popular.host
        val keyword = popular.scale

        // triggered by a refresh, we better execute sync
        try {
            val response = request.execute()
            var data = response.body()?: mutableListOf()
            if (popular.safe_mode) {
                val tmp: MutableList<PostSankaku> = mutableListOf()
                data.forEach {
                    if (it.rating == "s") {
                        tmp.add(it)
                    }
                }
                data = tmp
            }
            db.postSankakuDao().deletePosts(host, keyword)
            val start = db.postSankakuDao().getNextIndex(host = host, keyword = keyword)
            val items = data.mapIndexed { index, post ->
                post.scheme = scheme
                post.host = host
                post.keyword = keyword
                post.indexInResponse = start + index
                post
            }
            db.postSankakuDao().insert(items)
            retry = null
            networkState.postValue(NetworkState.LOADED)
            initialLoad.postValue(NetworkState.LOADED)
            callback.onResult(items, null, null)
        } catch (ioException: IOException) {
            retry = {
                loadInitial(params, callback)
            }
            val error = NetworkState.error(ioException.message ?: "unknown error")
            networkState.postValue(error)
            initialLoad.postValue(error)
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, PostSankaku>) {

    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, PostSankaku>) {

    }
}