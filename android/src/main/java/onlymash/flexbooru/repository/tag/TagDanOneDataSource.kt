/*
 * Copyright (C) 2019. by onlymash <im@fiepi.me>, All rights reserved
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package onlymash.flexbooru.repository.tag

import onlymash.flexbooru.api.url.DanOneUrlHelper
import onlymash.flexbooru.api.DanbooruOneApi
import onlymash.flexbooru.entity.tag.SearchTag
import onlymash.flexbooru.entity.tag.TagDanOne
import onlymash.flexbooru.repository.BasePageKeyedDataSource
import onlymash.flexbooru.repository.NetworkState
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.Executor

/**
 * Danbooru1.x tags data source that uses the before/after keys returned in page requests.
 */
class TagDanOneDataSource(private val danbooruOneApi: DanbooruOneApi,
                          private val search: SearchTag,
                          retryExecutor: Executor) : BasePageKeyedDataSource<Int, TagDanOne>(retryExecutor) {

    override fun loadInitialRequest(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, TagDanOne>) {
        val request = danbooruOneApi.getTags(DanOneUrlHelper.getTagUrl(search = search, page = 1))
        val response =  request.execute()
        val data = response.body() ?: mutableListOf()
        if (data.size < search.limit) {
            callback.onResult(data, null, null)
            onEnd()
        } else {
            callback.onResult(data, null, 2)
        }
    }

    override fun loadAfterRequest(params: LoadParams<Int>, callback: LoadCallback<Int, TagDanOne>) {
        networkState.postValue(NetworkState.LOADING)
        val page = params.key
        danbooruOneApi.getTags(DanOneUrlHelper.getTagUrl(search = search, page = page))
            .enqueue(object : retrofit2.Callback<MutableList<TagDanOne>> {
                override fun onFailure(call: Call<MutableList<TagDanOne>>, t: Throwable) {
                    loadAfterOnFailed(t.message ?: "unknown err", params, callback)
                }
                override fun onResponse(call: Call<MutableList<TagDanOne>>, response: Response<MutableList<TagDanOne>>) {
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