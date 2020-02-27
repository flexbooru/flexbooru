package onlymash.flexbooru.repository.popular

import androidx.paging.PagedList
import androidx.paging.PagingRequestHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.api.DanbooruOneApi
import onlymash.flexbooru.api.url.DanOneUrlHelper
import onlymash.flexbooru.entity.post.PostDanOne
import onlymash.flexbooru.entity.post.SearchPopular
import onlymash.flexbooru.extension.NetResult
import onlymash.flexbooru.extension.createStatusLiveData
import retrofit2.Response
import java.util.concurrent.Executor

class PopularDanOneBoundaryCallback(
    private val scope: CoroutineScope,
    private val danbooruOneApi: DanbooruOneApi,
    private val handleResponse: (SearchPopular, MutableList<PostDanOne>?) -> Unit,
    private val search: SearchPopular,
    ioExecutor: Executor
) : PagedList.BoundaryCallback<PostDanOne>() {

    //PagingRequestHelper
    val helper = PagingRequestHelper(ioExecutor)
    //network state
    val networkState = helper.createStatusLiveData()

    private suspend fun insertItemsIntoDb(
        response: Response<MutableList<PostDanOne>>,
        it: PagingRequestHelper.Request.Callback) {
        withContext(Dispatchers.IO) {
            it.recordSuccess()
            handleResponse(search, response.body())
        }
    }

    private fun createCallback(it: PagingRequestHelper.Request.Callback) {
        scope.launch {
            when (val result = withContext(Dispatchers.IO) {
                try {
                    val response = danbooruOneApi.getPosts(DanOneUrlHelper.getPopularUrl(search))
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

    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            createCallback(it)
        }
    }

    override fun onItemAtEndLoaded(itemAtEnd: PostDanOne) {

    }

    override fun onItemAtFrontLoaded(itemAtFront: PostDanOne) {

    }
}