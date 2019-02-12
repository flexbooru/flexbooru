package onlymash.flexbooru.repository.post

import androidx.annotation.MainThread
import androidx.paging.PagedList
import androidx.paging.PagingRequestHelper
import onlymash.flexbooru.api.ApiUrlHelper
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.entity.PostDan
import onlymash.flexbooru.entity.SearchPost
import onlymash.flexbooru.util.createStatusLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

class PostDanBoundaryCallback(
    private val danbooruApi: DanbooruApi,
    private val handleResponse: (SearchPost, MutableList<PostDan>?) -> Unit,
    private val ioExecutor: Executor,
    private val search: SearchPost
) : PagedList.BoundaryCallback<PostDan>() {

    val helper = PagingRequestHelper(ioExecutor)
    val networkState = helper.createStatusLiveData()

    var lastResponseSize = search.limit

    private fun insertItemsIntoDb(response: Response<MutableList<PostDan>>, it: PagingRequestHelper.Request.Callback) {
        ioExecutor.execute {
            lastResponseSize = if (!response.body().isNullOrEmpty()) response.body()?.size!! else 0
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
            danbooruApi.getPosts(ApiUrlHelper.getDanUrl(search, 1)).enqueue(createDanbooruCallback(it))
        }
    }

    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: PostDan) {
        val indexInNext = itemAtEnd.indexInResponse + 1
        val limit = search.limit
        if (lastResponseSize == limit) {
            helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
                danbooruApi.getPosts(ApiUrlHelper.getDanUrl(search, indexInNext/limit + 1))
                    .enqueue(createDanbooruCallback(it))
            }
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: PostDan) {
        // ignored, since we only ever append to what's in the DB
    }
}