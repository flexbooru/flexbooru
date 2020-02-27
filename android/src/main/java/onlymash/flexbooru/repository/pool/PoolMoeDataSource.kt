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

import onlymash.flexbooru.api.url.MoeUrlHelper
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.entity.pool.PoolMoe
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.repository.BasePageKeyedDataSource
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.Executor

//moebooru pools data source
class PoolMoeDataSource(private val moebooruApi: MoebooruApi,
                        private val search: Search,
                        retryExecutor: Executor) : BasePageKeyedDataSource<Int, PoolMoe>(retryExecutor) {

    private val pageSize = 20

    override fun loadInitialRequest(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, PoolMoe>) {
        val scheme = search.scheme
        val host = search.host
        val keyword = search.keyword
        val response =
            moebooruApi.getPools(MoeUrlHelper.getPoolUrl(search = search, page = 1)).execute()
        val data = response.body() ?: mutableListOf()
        data.forEach {
            it.scheme = scheme
            it.host = host
            it.keyword = keyword
        }
        if (data.size < pageSize) {
            onEnd()
            callback.onResult(data, null, null)
        } else {
            callback.onResult(data, null, 2)
        }
    }

    override fun loadAfterRequest(params: LoadParams<Int>, callback: LoadCallback<Int, PoolMoe>) {
        val page = params.key
        moebooruApi.getPools(MoeUrlHelper.getPoolUrl(search = search, page = page))
            .enqueue(object : retrofit2.Callback<MutableList<PoolMoe>> {
                override fun onFailure(call: Call<MutableList<PoolMoe>>, t: Throwable) {
                    loadAfterOnFailed(t.message ?: "unknown err", params, callback)
                }
                override fun onResponse(call: Call<MutableList<PoolMoe>>, response: Response<MutableList<PoolMoe>>) {
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
                        if (data.size < pageSize) {
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