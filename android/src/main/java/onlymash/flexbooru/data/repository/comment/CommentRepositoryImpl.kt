/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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

package onlymash.flexbooru.data.repository.comment

import androidx.annotation.MainThread
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.toLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.app.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.data.action.ActionComment
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.model.common.Comment
import onlymash.flexbooru.data.model.sankaku.CommentBody
import onlymash.flexbooru.data.repository.Listing
import onlymash.flexbooru.extension.NetResult

class CommentRepositoryImpl(private val booruApis: BooruApis) : CommentRepository {

    @MainThread
    override fun getComments(scope: CoroutineScope, action: ActionComment): Listing<Comment> {
        val sourceFactory = CommentDataSourceFactory(action, booruApis, scope)
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = action.limit,
                enablePlaceholders = true
            )
        )
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

    override suspend fun createComment(action: ActionComment): NetResult<Boolean> {
        return when (action.booru.type) {
            BOORU_TYPE_DAN -> createDanComment(action)
            BOORU_TYPE_DAN1 -> createDan1Comment(action)
            BOORU_TYPE_MOE -> createMoeComment(action)
            else -> createSankakuComment(action)
        }
    }

    override suspend fun destroyComment(action: ActionComment): NetResult<Boolean> {
        return when (action.booru.type) {
            BOORU_TYPE_DAN -> destroyDanComment(action)
            BOORU_TYPE_DAN1 -> destroyDan1Comment(action)
            BOORU_TYPE_MOE -> destroyMoeComment(action)
            else -> destroySankakuComment(action)
        }
    }
    
    private suspend fun createDanComment(action: ActionComment): NetResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.danApi.createComment(
                    url = action.getDanCreateCommentUrl(),
                    body = action.body,
                    anonymous = action.anonymous,
                    username = action.booru.user?.name ?: "",
                    apiKey = action.booru.user?.token ?: "",
                    postId = action.postId
                )
                if (response.isSuccessful) {
                    NetResult.Success(true)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun createDan1Comment(action: ActionComment): NetResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.dan1Api.createComment(
                    url = action.getDan1CreateCommentUrl(),
                    body = action.body,
                    anonymous = action.anonymous,
                    username = action.booru.user?.name ?: "",
                    passwordHash = action.booru.user?.token ?: "",
                    postId = action.postId
                )
                if (response.isSuccessful) {
                    NetResult.Success(true)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun createMoeComment(action: ActionComment): NetResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.moeApi.createComment(
                    url = action.getMoeCreateCommentUrl(),
                    postId = action.postId,
                    body = action.body,
                    anonymous = action.anonymous,
                    username = action.booru.user?.name ?: "",
                    passwordHash = action.booru.user?.token ?: ""
                )
                if (response.isSuccessful) {
                    NetResult.Success(true)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun createSankakuComment(action: ActionComment): NetResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.sankakuApi.createComment(
                    url = action.getSankakuCreateCommentUrl(),
                    comment = CommentBody.createBody(action.body),
                    auth = action.booru.user?.getAuth.toString())
                if (response.isSuccessful) {
                    NetResult.Success(true)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun destroyDanComment(action: ActionComment): NetResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.danApi.deleteComment(
                    url = action.getDanDeleteCommentUrl(),
                    username = action.booru.user?.name ?: "",
                    apiKey = action.booru.user?.token ?: ""
                )
                if (response.isSuccessful) {
                    NetResult.Success(true)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun destroyDan1Comment(action: ActionComment): NetResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.dan1Api.destroyComment(
                    url = action.getDan1DestroyCommentUrl(),
                    commentId = action.commentId,
                    username = action.booru.user?.name ?: "",
                    passwordHash = action.booru.user?.token ?: ""
                )
                if (response.isSuccessful) {
                    NetResult.Success(true)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun destroyMoeComment(action: ActionComment): NetResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.moeApi.destroyComment(
                    url = action.getMoeDestroyCommentUrl(),
                    commentId = action.commentId,
                    username = action.booru.user?.name ?: "",
                    passwordHash = action.booru.user?.token ?: ""
                )
                if (response.isSuccessful) {
                    NetResult.Success(true)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun destroySankakuComment(action: ActionComment): NetResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.sankakuApi.destroyComment(
                    url = action.getSankakuDestroyCommentUrl(),
                    auth = action.booru.user?.getAuth.toString())
                if (response.isSuccessful) {
                    NetResult.Success(true)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }
}