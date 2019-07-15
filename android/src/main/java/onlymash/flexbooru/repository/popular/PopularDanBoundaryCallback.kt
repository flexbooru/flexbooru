package onlymash.flexbooru.repository.popular

import androidx.paging.PagedList
import androidx.paging.PagingRequestHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.R
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.url.DanUrlHelper
import onlymash.flexbooru.common.App
import onlymash.flexbooru.entity.post.PostDan
import onlymash.flexbooru.entity.post.SearchPopular
import onlymash.flexbooru.extension.NetResult
import onlymash.flexbooru.extension.createStatusLiveData
import retrofit2.Response
import java.util.concurrent.Executor

class PopularDanBoundaryCallback(
    private val scope: CoroutineScope,
    private val danbooruApi: DanbooruApi,
    private val handleResponse: (SearchPopular, MutableList<PostDan>?) -> Unit,
    private val search: SearchPopular,
    ioExecutor: Executor
) : PagedList.BoundaryCallback<PostDan>() {

    //PagingRequestHelper
    val helper = PagingRequestHelper(ioExecutor)
    //network state
    val networkState = helper.createStatusLiveData()

    private suspend fun insertItemsIntoDb(
        response: Response<MutableList<PostDan>>,
        it: PagingRequestHelper.Request.Callback) {
        withContext(Dispatchers.IO) {
            handleResponse(search, response.body())
            it.recordSuccess()
        }
    }

    private fun createCallback(it: PagingRequestHelper.Request.Callback) {
        scope.launch {
            when (val result = withContext(Dispatchers.IO) {
                try {
                    val response = danbooruApi.getPosts(DanUrlHelper.getPopularUrl(search))
                    NetResult.Success(response)
                } catch (e: Exception) {
                    NetResult.Error(e.message.toString())
                }
            }) {
                is NetResult.Success -> {
                    val response = result.data
                    if (response.code() == 401) {
                        it.recordFailure(Throwable(App.app.getString(R.string.msg_your_api_key_is_wrong)))
                    } else {
                        insertItemsIntoDb(response, it)
                    }
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

    override fun onItemAtEndLoaded(itemAtEnd: PostDan) {

    }

    override fun onItemAtFrontLoaded(itemAtFront: PostDan) {

    }
}