package onlymash.flexbooru.repository.post

import androidx.annotation.MainThread
import androidx.paging.PagedList
import androidx.paging.PagingRequestHelper
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.getDanbooruUrl
import onlymash.flexbooru.model.PostDan
import onlymash.flexbooru.model.Search
import onlymash.flexbooru.util.createStatusLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

class PostDanBoundaryCallback(
    private val danbooruApi: DanbooruApi,
    private val handleResponse: (Search, MutableList<PostDan>?) -> Unit,
    private val ioExecutor: Executor,
    private val search: Search
) : PagedList.BoundaryCallback<PostDan>() {

    val helper = PagingRequestHelper(ioExecutor)
    val networkState = helper.createStatusLiveData()

    private fun insertItemsIntoDb(response: Response<MutableList<PostDan>>, it: PagingRequestHelper.Request.Callback) {
        ioExecutor.execute {
            handleResponse(search, response.body())
            it.recordSuccess()
        }
    }

    private fun createDanbooruCallback(it: PagingRequestHelper.Request.Callback)
            : Callback<MutableList<PostDan>> {
        return object : Callback<MutableList<PostDan>> {
            override fun onFailure(
                call: Call<MutableList<PostDan>>,
                t: Throwable) {
                it.recordFailure(t)
            }

            override fun onResponse(
                call: Call<MutableList<PostDan>>,
                response: Response<MutableList<PostDan>>
            ) {
                insertItemsIntoDb(response, it)
            }
        }
    }

    @MainThread
    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            danbooruApi.getPosts(getDanbooruUrl(search, 1)).enqueue(createDanbooruCallback(it))
        }
    }

    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: PostDan) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
            danbooruApi.getPosts(getDanbooruUrl(search, (itemAtEnd.indexInResponse + 1)/search.limit + 1)).enqueue(createDanbooruCallback(it))
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: PostDan) {
        // ignored, since we only ever append to what's in the DB
    }
}