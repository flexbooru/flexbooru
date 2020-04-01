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

package onlymash.flexbooru.data.repository.favorite

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import onlymash.flexbooru.common.App
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.common.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.common.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.data.action.ActionVote
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.database.dao.PostDao
import onlymash.flexbooru.extension.NetResult
import retrofit2.HttpException

class VoteRepositoryImpl(
    private val booruApis: BooruApis,
    private val postDao: PostDao
): VoteRepository {

    override suspend fun addFav(action: ActionVote): NetResult<Boolean> {
        return when (action.booru.type) {
            BOORU_TYPE_DAN -> addDanFav(action)
            BOORU_TYPE_DAN1 -> addDan1Fav(action)
            BOORU_TYPE_MOE -> voteMoePost(action, 3)
            BOORU_TYPE_GEL -> addGelFav(action)
            else -> addSankakuFav(action)
        }
    }

    override suspend fun removeFav(action: ActionVote): NetResult<Boolean> {
        return when (action.booru.type) {
            BOORU_TYPE_DAN -> removeDanFav(action)
            BOORU_TYPE_DAN1 -> removeDan1Fav(action)
            BOORU_TYPE_MOE -> voteMoePost(action, 0)
            BOORU_TYPE_GEL -> removeGelFav()
            else -> removeSankakuFav(action)
        }
    }

    private suspend fun addGelFav(action: ActionVote): NetResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.gelApi.favPost(action.getGelAddFavUrl())
                if (response.isSuccessful) {
                    postDao.updateFav(booruUid = action.booru.uid, postId = action.postId, isFavored = true)
                    NetResult.Success(true)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private fun removeGelFav(): NetResult<Boolean> {
        return NetResult.Error("Not supported")
    }

    private suspend fun voteMoePost(action: ActionVote, score: Int): NetResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.moeApi.votePost(
                    url = action.getMoeVoteUrl(),
                    id = action.postId,
                    score = score,
                    username = action.booru.user?.name ?: "",
                    passwordHash = action.booru.user?.token ?: ""
                )
                if (response.isSuccessful) {
                    postDao.updateFav(booruUid = action.booru.uid, postId = action.postId, isFavored = score != 0)
                    NetResult.Success(true)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                if (e is HttpException) {
                    val code = e.code()
                    if (code == 403) {
                        NetResult.Error(App.app.getString(R.string.msg_password_or_hash_salt_wrong))
                    } else {
                        NetResult.Error("code: $code")
                    }
                } else NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun addDanFav(action: ActionVote): NetResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.danApi.favPost(
                    url = action.getDanAddFavUrl(),
                    id = action.postId,
                    username = action.booru.user?.name ?: "",
                    apiKey = action.booru.user?.token ?: ""
                )
                if (response.isSuccessful) {
                    postDao.updateFav(booruUid = action.booru.uid, postId = action.postId, isFavored = true)
                    NetResult.Success(true)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                if (e is HttpException) {
                    val code = e.code()
                    if (code == 500) {
                        postDao.updateFav(booruUid = action.booru.uid, postId = action.postId, isFavored = true)
                        NetResult.Success(true)
                    } else NetResult.Error("code: ${e.code()}")
                } else NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun removeDanFav(action: ActionVote): NetResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.danApi.removeFavPost(action.getDanRemoveFavUrl())
                if (response.isSuccessful) {
                    postDao.updateFav(booruUid = action.booru.uid, postId = action.postId, isFavored = false)
                    NetResult.Success(true)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                if (e is HttpException)
                    NetResult.Error("code: ${e.code()}")
                else
                    NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun addDan1Fav(action: ActionVote): NetResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.dan1Api.favPost(
                    url = action.getDan1RemoveFavUrl(),
                    id = action.postId,
                    username = action.booru.user?.name ?: "",
                    passwordHash = action.booru.user?.token ?: ""
                )
                if (response.isSuccessful) {
                    postDao.updateFav(booruUid = action.booru.uid, postId = action.postId, isFavored = true)
                    NetResult.Success(true)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                if (e is HttpException)
                    NetResult.Error("code: ${e.code()}")
                else
                    NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun removeDan1Fav(action: ActionVote): NetResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.dan1Api.removeFavPost(
                    url = action.getDan1RemoveFavUrl(),
                    postId = action.postId,
                    username = action.booru.user?.name ?: "",
                    passwordHash = action.booru.user?.token ?: ""
                )
                if (response.isSuccessful) {
                    postDao.updateFav(booruUid = action.booru.uid, postId = action.postId, isFavored = false)
                    NetResult.Success(true)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                if (e is HttpException)
                    NetResult.Error("code: ${e.code()}")
                else
                    NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun addSankakuFav(action: ActionVote): NetResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.sankakuApi.favPost(
                    url = action.getSankakuAddFavUrl(),
                    postId = action.postId,
                    username = action.booru.user?.name ?: "",
                    passwordHash = action.booru.user?.token ?: ""
                )
                if (response.isSuccessful || response.code() == 423) {
                    postDao.updateFav(booruUid = action.booru.uid, postId = action.postId, isFavored = true)
                    NetResult.Success(true)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                if (e is HttpException)
                    NetResult.Error("code: ${e.code()}")
                else
                    NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun removeSankakuFav(action: ActionVote): NetResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.sankakuApi.removeFavPost(
                    url = action.getSankakuRemoveFavUrl(),
                    postId = action.postId,
                    username = action.booru.user?.name ?: "",
                    passwordHash = action.booru.user?.token ?: ""
                )
                if (response.isSuccessful) {
                    postDao.updateFav(booruUid = action.booru.uid, postId = action.postId, isFavored = false)
                    NetResult.Success(true)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                if (e is HttpException)
                    NetResult.Error("code: ${e.code()}")
                else
                    NetResult.Error(e.message.toString())
            }
        }
    }
}