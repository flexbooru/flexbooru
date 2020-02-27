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

package onlymash.flexbooru.repository.pool

import onlymash.flexbooru.api.DanbooruOneApi
import onlymash.flexbooru.api.url.DanOneUrlHelper
import onlymash.flexbooru.entity.pool.PoolDanOne
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.repository.BasePageKeyedDataSource
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.Executor

// danbooru1.x pools data source
class PoolDanOneDataSource(private val danbooruOneApi: DanbooruOneApi,
                        private val search: Search,
                        retryExecutor: Executor) : BasePageKeyedDataSource<Int, PoolDanOne>(retryExecutor) {
    companion object {
        const val PAGE_SIZE = 20
    }

    override fun loadInitialRequest(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, PoolDanOne>) {
        val request = danbooruOneApi.getPools(DanOneUrlHelper.getPoolUrl(search = search, page = 1))
        val scheme = search.scheme
        val host = search.host
        val keyword = search.keyword
        val response =  request.execute()
        val data = response.body() ?: mutableListOf()
        data.forEach {
            it.scheme = scheme
            it.host = host
            it.keyword = keyword
        }
        if (data.size < PAGE_SIZE) {
            onEnd()
            callback.onResult(data, null, null)
        } else {
            callback.onResult(data, null, 2)
        }
    }

    override fun loadAfterRequest(params: LoadParams<Int>, callback: LoadCallback<Int, PoolDanOne>) {
        val page = params.key
        danbooruOneApi.getPools(DanOneUrlHelper.getPoolUrl(search = search, page = page))
            .enqueue(object : retrofit2.Callback<MutableList<PoolDanOne>> {
                override fun onFailure(call: Call<MutableList<PoolDanOne>>, t: Throwable) {
                    loadAfterOnFailed(t.message ?: "unknown err", params, callback)
                }
                override fun onResponse(call: Call<MutableList<PoolDanOne>>, response: Response<MutableList<PoolDanOne>>) {
                    if (response.isSuccessful) {
                        val data = response.body() ?: mutableListOf()
                        val scheme = search.scheme
                        val host = search.host
                        val keyword = search.keyword
                        data.forEach {
                            it.scheme = scheme
                            it.host = host
                            it.keyword = keyword
                        }
                        loadAfterOnSuccess()
                        if (data.size < PAGE_SIZE) {
                            onEnd()
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