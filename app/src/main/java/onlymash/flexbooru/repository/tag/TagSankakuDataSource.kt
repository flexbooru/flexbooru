package onlymash.flexbooru.repository.tag

import onlymash.flexbooru.api.SankakuApi
import onlymash.flexbooru.api.url.SankakuUrlHelper
import onlymash.flexbooru.entity.tag.TagSankaku
import onlymash.flexbooru.entity.tag.SearchTag
import onlymash.flexbooru.repository.BasePageKeyedDataSource
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.Executor

/**
 * Sankaku tags data source that uses the before/after keys returned in page requests.
 */
class TagSankakuDataSource(private val sankakuApi: SankakuApi,
                       private val search: SearchTag,
                       retryExecutor: Executor) : BasePageKeyedDataSource<Int, TagSankaku>(retryExecutor) {

    override fun loadInitialRequest(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, TagSankaku>)  {
        val request = sankakuApi.getTags(SankakuUrlHelper.getTagUrl(search = search, page = 1))
        val response =  request.execute()
        val data = response.body() ?: mutableListOf()
        if (data.size < search.limit) {
            callback.onResult(data, null, null)
            onEnd()
        } else {
            callback.onResult(data, null, 2)
        }
    }

    override fun loadAfterRequest(params: LoadParams<Int>, callback: LoadCallback<Int, TagSankaku>) {
        val page = params.key
        sankakuApi.getTags(SankakuUrlHelper.getTagUrl(search = search, page = page))
            .enqueue(object : retrofit2.Callback<MutableList<TagSankaku>> {
                override fun onFailure(call: Call<MutableList<TagSankaku>>, t: Throwable) {
                    loadAfterOnFailed(t.message ?: "unknown err", params, callback)
                }
                override fun onResponse(call: Call<MutableList<TagSankaku>>, response: Response<MutableList<TagSankaku>>) {
                    if (response.isSuccessful) {
                        val data = response.body() ?: mutableListOf()
                        loadAfterOnSuccess()
                        if (data.size < search.limit) {
                            callback.onResult(data, null)
                            onEnd()
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