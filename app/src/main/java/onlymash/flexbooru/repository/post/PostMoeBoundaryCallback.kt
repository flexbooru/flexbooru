package onlymash.flexbooru.repository.post

import androidx.annotation.MainThread
import androidx.paging.PagedList
import androidx.paging.PagingRequestHelper
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.api.getMoebooruUrl
import onlymash.flexbooru.model.PostMoe
import onlymash.flexbooru.model.Search
import onlymash.flexbooru.util.createStatusLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

class PostMoeBoundaryCallback(
    private val moebooruApi: MoebooruApi,
    private val handleResponse: (Search, MutableList<PostMoe>?) -> Unit,
    private val ioExecutor: Executor,
    private val search: Search
) : PagedList.BoundaryCallback<PostMoe>() {

    val helper = PagingRequestHelper(ioExecutor)
    val networkState = helper.createStatusLiveData()

    var lastResponseSize = search.limit

    private fun insertItemsIntoDb(response: Response<MutableList<PostMoe>>, it: PagingRequestHelper.Request.Callback) {
        ioExecutor.execute {
            lastResponseSize = if (!response.body().isNullOrEmpty()) response.body()?.size!! else 0
            handleResponse(search, response.body())
            it.recordSuccess()
        }
    }

    private fun createMoebooruCallback(it: PagingRequestHelper.Request.Callback)
            : Callback<MutableList<PostMoe>> {
        return object : Callback<MutableList<PostMoe>> {
            override fun onFailure(
                call: Call<MutableList<PostMoe>>,
                t: Throwable) {
                it.recordFailure(t)
            }

            override fun onResponse(
                call: Call<MutableList<PostMoe>>,
                response: Response<MutableList<PostMoe>>
            ) {
                insertItemsIntoDb(response, it)
            }
        }
    }

    @MainThread
    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            moebooruApi.getPosts(getMoebooruUrl(search, 1)).enqueue(createMoebooruCallback(it))
        }
    }

    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: PostMoe) {
        val indexInNext = itemAtEnd.indexInResponse + 1
        val limit = search.limit
        if (lastResponseSize == limit) {
            helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
                moebooruApi.getPosts(getMoebooruUrl(search, indexInNext/limit + 1))
                    .enqueue(createMoebooruCallback(it))
            }
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: PostMoe) {
        // ignored, since we only ever append to what's in the DB
    }
}