package onlymash.flexbooru.repository.comment

import okhttp3.HttpUrl
import onlymash.flexbooru.api.url.GelUrlHelper
import onlymash.flexbooru.api.GelbooruApi
import onlymash.flexbooru.entity.comment.CommentAction
import onlymash.flexbooru.entity.comment.CommentGel
import onlymash.flexbooru.entity.comment.CommentGelResponse
import onlymash.flexbooru.repository.BasePageKeyedDataSource
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.Executor

//Gelbooru comment data source
class CommentGelDataSource(private val gelbooruApi: GelbooruApi,
                           private val commentAction: CommentAction,
                           retryExecutor: Executor
) : BasePageKeyedDataSource<Int, CommentGel>(retryExecutor) {

    private fun getUrl(page: Int): HttpUrl = if (commentAction.post_id > 0) {
        GelUrlHelper.getPostCommentUrl(action = commentAction, page = page)
    } else {
        GelUrlHelper.getPostsCommentUrl(action = commentAction, page = page)
    }

    override fun loadInitialRequest(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, CommentGel>) {
        val url = getUrl(1)
        val request = gelbooruApi.getComments(url)
        val response = request.execute()
        val data = response.body()?.comments ?: mutableListOf()
        if (data.size < commentAction.limit) {
            callback.onResult(data, null, null)
        } else {
            callback.onResult(data, null, 2)
        }
    }

    override fun loadAfterRequest(params: LoadParams<Int>, callback: LoadCallback<Int, CommentGel>) {
        val page = params.key
        val url = getUrl(page)
        gelbooruApi.getComments(url)
            .enqueue(object : retrofit2.Callback<CommentGelResponse> {
                override fun onFailure(call: Call<CommentGelResponse>, t: Throwable) {
                    loadAfterOnFailed(t.message ?: "unknown err", params, callback)
                }

                override fun onResponse(
                    call: Call<CommentGelResponse>,
                    response: Response<CommentGelResponse>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()?.comments ?: mutableListOf()
                        loadAfterOnSuccess()
                        if (data.size < commentAction.limit) {
                            callback.onResult(data, null)
                        } else {
                            callback.onResult(data, page + 1)
                        }
                    } else {
                        loadAfterOnFailed("error code: ${response.code()}", params, callback)
                    }
                }

            })
    }
}