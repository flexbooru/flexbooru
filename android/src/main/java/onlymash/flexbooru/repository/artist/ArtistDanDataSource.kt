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

package onlymash.flexbooru.repository.artist

import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.url.DanUrlHelper
import onlymash.flexbooru.entity.artist.ArtistDan
import onlymash.flexbooru.entity.artist.SearchArtist
import onlymash.flexbooru.repository.BasePageKeyedDataSource
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.Executor

/**
 *Danbooru artists data source
 * */
class ArtistDanDataSource(private val danbooruApi: DanbooruApi,
                          private val search: SearchArtist,
                          retryExecutor: Executor) : BasePageKeyedDataSource<Int, ArtistDan>(retryExecutor) {

    override fun loadInitialRequest(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, ArtistDan>) {
        val request = danbooruApi.getArtists(DanUrlHelper.getArtistUrl(search = search, page = 1))
        val scheme = search.scheme
        val host = search.host
        val response =  request.execute()
        val data = response.body() ?: mutableListOf()
        data.forEach {
            it.scheme = scheme
            it.host = host
        }
        if (data.size < search.limit) {
            callback.onResult(data, null, null)
            onEnd()
        } else {
            callback.onResult(data, null, 2)
        }
    }

    override fun loadAfterRequest(params: LoadParams<Int>, callback: LoadCallback<Int, ArtistDan>) {
        val page = params.key
        danbooruApi.getArtists(DanUrlHelper.getArtistUrl(search = search, page = page))
            .enqueue(object : retrofit2.Callback<MutableList<ArtistDan>> {
                override fun onFailure(call: Call<MutableList<ArtistDan>>, t: Throwable) {
                    loadAfterOnFailed(t.message ?: "unknown err", params, callback)
                }
                override fun onResponse(call: Call<MutableList<ArtistDan>>, response: Response<MutableList<ArtistDan>>) {
                    if (response.isSuccessful) {
                        val data = response.body() ?: mutableListOf()
                        val scheme = search.scheme
                        val host = search.host
                        data.forEach {
                            it.scheme = scheme
                            it.host = host
                        }
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