/*
 * Copyright (C) 2019 by onlymash <im@fiepi.me>, All rights reserved
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

package onlymash.flexbooru.repository.artist

import onlymash.flexbooru.api.DanbooruOneApi
import onlymash.flexbooru.api.url.DanOneUrlHelper
import onlymash.flexbooru.entity.ArtistDanOne
import onlymash.flexbooru.entity.SearchArtist
import onlymash.flexbooru.repository.BasePageKeyedDataSource
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.Executor

class ArtistDanOneDataSource(private val danbooruOneApi: DanbooruOneApi,
                             private val search: SearchArtist,
                             retryExecutor: Executor
) : BasePageKeyedDataSource<Int, ArtistDanOne>(retryExecutor) {
    override fun loadInitialRequest(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, ArtistDanOne>) {
        val request = danbooruOneApi.getArtists(DanOneUrlHelper.getArtistUrl(search = search, page = 1))
        val scheme = search.scheme
        val host = search.host
        val response =  request.execute()
        val data = response.body() ?: mutableListOf()
        data.forEach {
            it.scheme = scheme
            it.host = host
        }
        if (data.size < 25) {
            callback.onResult(data, null, null)
        } else {
            callback.onResult(data, null, 2)
        }
    }

    override fun loadAfterRequest(params: LoadParams<Int>, callback: LoadCallback<Int, ArtistDanOne>) {
        val page = params.key
        danbooruOneApi.getArtists(DanOneUrlHelper.getArtistUrl(search = search, page = page))
            .enqueue(object : retrofit2.Callback<MutableList<ArtistDanOne>> {
                override fun onFailure(call: Call<MutableList<ArtistDanOne>>, t: Throwable) {
                    loadAfterOnFailed(t.message ?: "unknown err", params, callback)
                }
                override fun onResponse(call: Call<MutableList<ArtistDanOne>>, response: Response<MutableList<ArtistDanOne>>) {
                    if (response.isSuccessful) {
                        val data = response.body() ?: mutableListOf()
                        val scheme = search.scheme
                        val host = search.host
                        data.forEach {
                            it.scheme = scheme
                            it.host = host
                        }
                        loadAfterOnSuccess()
                        if (data.size < 25) {
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