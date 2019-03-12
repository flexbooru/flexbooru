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

import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.toLiveData
import onlymash.flexbooru.api.url.MoeUrlHelper
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.DanbooruOneApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.api.url.DanOneUrlHelper
import onlymash.flexbooru.api.url.DanUrlHelper
import onlymash.flexbooru.entity.comment.*
import onlymash.flexbooru.repository.Listing
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.Executor

//Comment data repository
class CommentData(private val danbooruApi: DanbooruApi,
                  private val danbooruOneApi: DanbooruOneApi,
                  private val moebooruApi: MoebooruApi,
                  private val networkExecutor: Executor
) : CommentRepository {
    companion object {
        private const val TAG = "CommentData"
    }

    override val commentState: MutableLiveData<CommentState> = MutableLiveData()

    @MainThread
    override fun getDanComments(commentAction: CommentAction): Listing<CommentDan> {
        val sourceFactory = CommentDanDataSourceFactory(
            danbooruApi = danbooruApi,
            commentAction = commentAction,
            retryExecutor = networkExecutor
        )
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = commentAction.limit,
                enablePlaceholders = true
            ),
            fetchExecutor = networkExecutor)
        val refreshState =
            Transformations.switchMap(sourceFactory.sourceLiveData) { it.initialLoad }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { it.networkState },
            retry = { sourceFactory.sourceLiveData.value?.retryAllFailed() },
            refresh = { sourceFactory.sourceLiveData.value?.invalidate() },
            refreshState = refreshState
        )
    }

    @MainThread
    override fun createDanComment(commentAction: CommentAction) {
        danbooruApi.createComment(
            url = DanUrlHelper.getCreateCommentUrl(commentAction),
            body = commentAction.body,
            anonymous = commentAction.anonymous,
            username = commentAction.username,
            apiKey = commentAction.auth_key,
            postId = commentAction.post_id).enqueue(object : retrofit2.Callback<CommentResponse> {
            override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                commentState.postValue(CommentState.error(t.message ?: "unknown error"))
            }
            override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                if (response.isSuccessful) {
                    commentState.postValue(CommentState.SUCCESS)
                } else {
                    commentState.postValue(CommentState.error("error code: ${response.code()}"))
                }
            }
        })
    }

    @MainThread
    override fun destroyDanComment(commentAction: CommentAction) {
        danbooruApi.deleteComment(
            url = DanUrlHelper.getDeleteCommentUrl(commentAction),
            username = commentAction.username,
            apiKey = commentAction.auth_key
        )
            .enqueue(object : retrofit2.Callback<CommentResponse> {
                override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                    commentState.postValue(CommentState.error(t.message ?: "unknown error"))
                }
                override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                    if (response.isSuccessful) {
                        commentState.postValue(CommentState.SUCCESS)
                    } else {
                        commentState.postValue(CommentState.error("error code: ${response.code()}"))
                    }
                }
            })
    }

    @MainThread
    override fun getMoeComments(commentAction: CommentAction): Listing<CommentMoe> {
        val sourceFactory = CommentMoeDataSourceFactory(
            moebooruApi = moebooruApi,
            commentAction = commentAction,
            retryExecutor = networkExecutor)
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = 30,
                enablePlaceholders = true
            ),
            fetchExecutor = networkExecutor)
        val refreshState =
            Transformations.switchMap(sourceFactory.sourceLiveData) { it.initialLoad }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { it.networkState },
            retry = { sourceFactory.sourceLiveData.value?.retryAllFailed() },
            refresh = { sourceFactory.sourceLiveData.value?.invalidate() },
            refreshState = refreshState
        )
    }

    @MainThread
    override fun createMoeComment(commentAction: CommentAction) {
        moebooruApi.createComment(
            url = MoeUrlHelper.getCreateCommentUrl(commentAction),
            postId = commentAction.post_id,
            body = commentAction.body,
            anonymous = commentAction.anonymous,
            username = commentAction.username,
            passwordHash = commentAction.auth_key
        ).enqueue(object : retrofit2.Callback<CommentResponse> {
            override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                commentState.postValue(CommentState.error(t.message ?: "unknown error"))
            }
            override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                if (response.isSuccessful) {
                    commentState.postValue(CommentState.SUCCESS)
                } else {
                    commentState.postValue(CommentState.error("error code: ${response.code()}"))
                }
            }
        })
    }

    @MainThread
    override fun destroyMoeComment(commentAction: CommentAction) {
        moebooruApi.destroyComment(
            url = MoeUrlHelper.getDestroyCommentUrl(commentAction),
            commentId = commentAction.comment_id,
            username = commentAction.username,
            passwordHash = commentAction.auth_key).enqueue(object : retrofit2.Callback<CommentResponse> {
            override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                commentState.postValue(CommentState.error(t.message ?: "unknown error"))
            }
            override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                if (response.isSuccessful) {
                    commentState.postValue(CommentState.SUCCESS)
                } else {
                    commentState.postValue(CommentState.error("error code: ${response.code()}"))
                }
            }
        })
    }

    @MainThread
    override fun getDanOneComments(commentAction: CommentAction): Listing<CommentDanOne> {
        val sourceFactory = CommentDanOneDataSourceFactory(
            danbooruOneApi = danbooruOneApi,
            commentAction = commentAction,
            retryExecutor = networkExecutor
        )
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = commentAction.limit,
                enablePlaceholders = true
            ),
            fetchExecutor = networkExecutor)
        val refreshState =
            Transformations.switchMap(sourceFactory.sourceLiveData) { it.initialLoad }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { it.networkState },
            retry = { sourceFactory.sourceLiveData.value?.retryAllFailed() },
            refresh = { sourceFactory.sourceLiveData.value?.invalidate() },
            refreshState = refreshState
        )
    }

    @MainThread
    override fun createDanOneComment(commentAction: CommentAction) {
        danbooruOneApi.createComment(
            url = DanOneUrlHelper.getCreateCommentUrl(commentAction),
            body = commentAction.body,
            anonymous = commentAction.anonymous,
            username = commentAction.username,
            passwordHash = commentAction.auth_key,
            postId = commentAction.post_id).enqueue(object : retrofit2.Callback<CommentResponse> {
            override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                commentState.postValue(CommentState.error(t.message ?: "unknown error"))
            }
            override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                if (response.isSuccessful) {
                    commentState.postValue(CommentState.SUCCESS)
                } else {
                    commentState.postValue(CommentState.error("error code: ${response.code()}"))
                }
            }
        })
    }

    @MainThread
    override fun destroyDanOneComment(commentAction: CommentAction) {
        danbooruOneApi.destroyComment(
            url = DanOneUrlHelper.getDestroyCommentUrl(commentAction),
            commentId = commentAction.comment_id,
            username = commentAction.username,
            passwordHash = commentAction.auth_key)
            .enqueue(object : retrofit2.Callback<CommentResponse> {
                override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                    commentState.postValue(CommentState.error(t.message ?: "unknown error"))
                }
                override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                    if (response.isSuccessful) {
                        commentState.postValue(CommentState.SUCCESS)
                    } else {
                        commentState.postValue(CommentState.error("error code: ${response.code()}"))
                    }
                }
            })
    }
}