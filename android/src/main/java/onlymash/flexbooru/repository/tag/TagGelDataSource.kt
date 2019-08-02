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

import onlymash.flexbooru.api.url.GelUrlHelper
import onlymash.flexbooru.api.GelbooruApi
import onlymash.flexbooru.entity.tag.SearchTag
import onlymash.flexbooru.entity.tag.TagGel
import onlymash.flexbooru.entity.tag.TagGelResponse
import onlymash.flexbooru.repository.BasePageKeyedDataSource
import onlymash.flexbooru.repository.NetworkState
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.Executor

/**
 * Gelbooru tags data source that uses the before/after keys returned in page requests.
 */
class TagGelDataSource(private val gelbooruApi: GelbooruApi,
                       private val search: SearchTag,
                       retryExecutor: Executor) : BasePageKeyedDataSource<Int, TagGel>(retryExecutor) {

    override fun loadInitialRequest(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, TagGel>) {
        val request = gelbooruApi.getTags(GelUrlHelper.getTagUrl(search = search, page = 0))
        val response =  request.execute()
        val data = response.body()?.tags ?: mutableListOf()
        if (data.size < search.limit) {
            callback.onResult(data, null, null)
            onEnd()
        } else {
            callback.onResult(data, null, 1)
        }
    }

    override fun loadAfterRequest(params: LoadParams<Int>, callback: LoadCallback<Int, TagGel>) {
        networkState.postValue(NetworkState.LOADING)
        val page = params.key
        gelbooruApi.getTags(GelUrlHelper.getTagUrl(search = search, page = page))
            .enqueue(object : retrofit2.Callback<TagGelResponse> {
                override fun onFailure(call: Call<TagGelResponse>, t: Throwable) {
                    loadAfterOnFailed(t.message ?: "unknown err", params, callback)
                }
                override fun onResponse(call: Call<TagGelResponse>, response: Response<TagGelResponse>) {
                    if (response.isSuccessful) {
                        val data = response.body()?.tags ?: mutableListOf()
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