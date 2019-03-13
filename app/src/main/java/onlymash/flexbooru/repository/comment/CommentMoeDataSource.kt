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

package onlymash.flexbooru.repository.comment

import okhttp3.HttpUrl
import onlymash.flexbooru.api.url.MoeUrlHelper
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.entity.comment.CommentAction
import onlymash.flexbooru.entity.comment.CommentMoe
import onlymash.flexbooru.repository.BasePageKeyedDataSource
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.Executor

//Moebooru comment data source
class CommentMoeDataSource(private val moebooruApi: MoebooruApi,
                           private val commentAction: CommentAction,
                           retryExecutor: Executor
) : BasePageKeyedDataSource<Int, CommentMoe>(retryExecutor) {

    private val pageSize = 30

    private fun getUrl(page: Int): HttpUrl = if (commentAction.post_id > 0) {
        MoeUrlHelper.getPostCommentUrl(commentAction = commentAction, page = page)
    } else {
        MoeUrlHelper.getPostsCommentUrl(commentAction = commentAction, page = page)
    }

    override fun loadInitialRequest(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, CommentMoe>) {
        val url = getUrl(1)
        val request = moebooruApi.getComments(url)
        val scheme = commentAction.scheme
        val host = commentAction.host
        val response = request.execute()
        val data = response.body() ?: mutableListOf()
        data.forEach {
            it.scheme = scheme
            it.host = host
        }
        if (data.size < pageSize) {
            callback.onResult(data, null, null)
        } else {
            callback.onResult(data, null, 2)
        }
    }

    override fun loadAfterRequest(params: LoadParams<Int>, callback: LoadCallback<Int, CommentMoe>) {
        val page = params.key
        val url = getUrl(page)
        moebooruApi.getComments(url)
            .enqueue(object : retrofit2.Callback<MutableList<CommentMoe>> {
                override fun onFailure(call: Call<MutableList<CommentMoe>>, t: Throwable) {
                    loadAfterOnFailed(t.message ?: "unknown err", params, callback)
                }

                override fun onResponse(
                    call: Call<MutableList<CommentMoe>>,
                    response: Response<MutableList<CommentMoe>>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body() ?: mutableListOf()
                        val scheme = commentAction.scheme
                        val host = commentAction.host
                        data.forEach {
                            it.scheme = scheme
                            it.host = host
                        }
                        loadAfterOnSuccess()
                        if (data.size < pageSize) {
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