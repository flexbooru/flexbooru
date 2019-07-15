package onlymash.flexbooru.repository.popular

import androidx.annotation.MainThread
import androidx.paging.PagedList
import androidx.paging.PagingRequestHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.api.SankakuApi
import onlymash.flexbooru.api.url.SankakuUrlHelper
import onlymash.flexbooru.entity.post.PostSankaku
import onlymash.flexbooru.entity.post.SearchPopular
import onlymash.flexbooru.extension.NetResult
import onlymash.flexbooru.extension.createStatusLiveData
import retrofit2.Response
import java.util.concurrent.Executor

/**
 * Sankaku posts data callback
 * This boundary callback gets notified when user reaches to the edges of the list such that the
 * database cannot provide any more data.
 * <p>
 * The boundary callback might be called multiple times for the same direction so it does its own
 * rate limiting using the PagingRequestHelper class.
 */
class PopularSankakuBoundaryCallback(
    private val scope: CoroutineScope,
    private val sankakuApi: SankakuApi,
    private val handleResponse: (SearchPopular, MutableList<PostSankaku>?) -> Unit,
    private val search: SearchPopular,
    ioExecutor: Executor
) : PagedList.BoundaryCallback<PostSankaku>() {

    //PagingRequestHelper
    val helper = PagingRequestHelper(ioExecutor)
    //network state
    val networkState = helper.createStatusLiveData()

    //last response posts size
    var lastResponseSize = search.limit

    private suspend fun insertItemsIntoDb(response: Response<MutableList<PostSankaku>>, it: PagingRequestHelper.Request.Callback) {
        withContext(Dispatchers.IO) {
            val data = response.body()
            lastResponseSize = data?.size ?: 0
            handleResponse(search, data)
            it.recordSuccess()
        }
    }

    private fun createCallback(page: Int, it: PagingRequestHelper.Request.Callback) {
        scope.launch {
            when (val result = withContext(Dispatchers.IO) {
                try {
                    val response = sankakuApi.getPosts(SankakuUrlHelper.getPopularUrl(search, page))
                    NetResult.Success(response)
                } catch (e: Exception) {
                    NetResult.Error(e.message.toString())
                }
            }) {
                is NetResult.Success -> {
                    insertItemsIntoDb(result.data, it)
                }
                is NetResult.Error -> {
                    it.recordFailure(Throwable(result.errorMsg))
                }
            }
        }
    }

    /**
     * Database returned 0 items. We should query the backend for more items.
     */
    @MainThread
    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            createCallback(1, it)
        }
    }

    /**
     * User reached to the end of the list.
     */
    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: PostSankaku) {
        val indexInNext = itemAtEnd.indexInResponse + 1
        val limit = search.limit
        if (lastResponseSize == limit) {
            helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
                createCallback(indexInNext/limit + 1, it)
            }
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: PostSankaku) {
        // ignored, since we only ever append to what's in the DB
    }
}