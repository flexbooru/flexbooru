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

package onlymash.flexbooru.repository.comment

import okhttp3.HttpUrl
import onlymash.flexbooru.api.url.SankakuUrlHelper
import onlymash.flexbooru.api.SankakuApi
import onlymash.flexbooru.entity.comment.CommentAction
import onlymash.flexbooru.entity.comment.CommentSankaku
import onlymash.flexbooru.repository.BasePageKeyedDataSource
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.Executor


class CommentSankakuDataSource(private val sankakuApi: SankakuApi,
                               private val commentAction: CommentAction,
                               retryExecutor: Executor
) : BasePageKeyedDataSource<Int, CommentSankaku>(retryExecutor) {

    private val pageSize = 25

    private fun getUrl(page: Int): HttpUrl =
        when {
            commentAction.post_id > 0 -> SankakuUrlHelper.getPostCommentUrl(commentAction = commentAction, page = page)
            commentAction.query.isNotEmpty() -> SankakuUrlHelper.getPostsCommentSearchUrl(commentAction = commentAction, page = page)
            else -> SankakuUrlHelper.getPostsCommentIndexUrl(commentAction = commentAction, page = page)
        }

    override fun loadInitialRequest(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, CommentSankaku>) {
        val url = getUrl(1)
        val request = sankakuApi.getComments(url)
        val response = request.execute()
        val data = response.body() ?: mutableListOf()
        data.forEach {
            it.host = commentAction.host
            it.scheme = commentAction.scheme
        }
        if (data.size < pageSize) {
            callback.onResult(data, null, null)
        } else {
            callback.onResult(data, null, 2)
        }
    }

    override fun loadAfterRequest(params: LoadParams<Int>, callback: LoadCallback<Int, CommentSankaku>) {
        val page = params.key
        val url = getUrl(page)
        sankakuApi.getComments(url)
            .enqueue(object : retrofit2.Callback<MutableList<CommentSankaku>> {
                override fun onFailure(call: Call<MutableList<CommentSankaku>>, t: Throwable) {
                    loadAfterOnFailed(t.message ?: "unknown err", params, callback)
                }
                override fun onResponse(
                    call: Call<MutableList<CommentSankaku>>,
                    response: Response<MutableList<CommentSankaku>>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body() ?: mutableListOf()
                        data.forEach {
                            it.host = commentAction.host
                            it.scheme = commentAction.scheme
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