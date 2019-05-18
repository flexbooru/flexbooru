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

package onlymash.flexbooru.repository.favorite

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import onlymash.flexbooru.App
import onlymash.flexbooru.R
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.DanbooruOneApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.api.SankakuApi
import onlymash.flexbooru.api.url.DanOneUrlHelper
import onlymash.flexbooru.api.url.DanUrlHelper
import onlymash.flexbooru.api.url.MoeUrlHelper
import onlymash.flexbooru.api.url.SankakuUrlHelper
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.entity.*
import onlymash.flexbooru.entity.post.PostDan
import onlymash.flexbooru.entity.post.PostDanOne
import onlymash.flexbooru.entity.post.PostSankaku
import onlymash.flexbooru.extension.NetResult
import retrofit2.HttpException

class VoteRepositoryImpl(private val danbooruApi: DanbooruApi,
                         private val danbooruOneApi: DanbooruOneApi,
                         private val moebooruApi: MoebooruApi,
                         private val sankakuApi: SankakuApi,
                         private val db: FlexbooruDatabase): VoteRepository {

    override suspend fun voteMoePost(vote: Vote): NetResult<VoteMoe> {
        return withContext(Dispatchers.IO) {
            try {
                val data = moebooruApi.votePostAsync(
                    url = MoeUrlHelper.getVoteUrl(vote),
                    id = vote.post_id,
                    score = vote.score,
                    username = vote.username,
                    passwordHash = vote.auth_key).await()
                if (data.success) {
                    if (vote.score == 0) {
                        db.runInTransaction {
                            db.postMoeDao().deletePost(
                                host = vote.host,
                                keyword = "vote:1:${vote.username} order:vote",
                                id = vote.post_id
                            )
                            db.postMoeDao().deletePost(
                                host = vote.host,
                                keyword = "vote:2:${vote.username} order:vote",
                                id = vote.post_id
                            )
                            db.postMoeDao().deletePost(
                                host = vote.host,
                                keyword = "vote:3:${vote.username} order:vote",
                                id = vote.post_id
                            )
                        }
                    } else {
                        val post= data.posts[0].apply {
                            scheme = vote.scheme
                            host = vote.host
                            keyword = "vote:${vote.score}:${vote.username} order:vote"
                        }
                        db.postMoeDao().insert(post)
                    }
                    NetResult.Success(data)
                } else NetResult.Error(data.message)
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

    override suspend fun addDanFav(vote: Vote, post: PostDan): NetResult<VoteDan> {
        return withContext(Dispatchers.IO) {
            try {
                val data = danbooruApi.favPostAsync(
                    url = DanUrlHelper.getAddFavUrl(vote),
                    id = vote.post_id,
                    username = vote.username,
                    apiKey = vote.auth_key
                ).await()
                post.scheme = vote.scheme
                post.keyword = "fav:${vote.username}"
                post.uid = 0L
                db.postDanDao().insert(post)
                NetResult.Success(data)
            } catch (e: Exception) {
                if (e is HttpException) {
                    val code = e.code()
                    if (code == 500) {
                        post.scheme = vote.scheme
                        post.keyword = "fav:${vote.username}"
                        post.uid = 0L
                        db.postDanDao().insert(post)
                        NetResult.Success(VoteDan(
                            success = true,
                            id = post.id
                        ))
                    } else NetResult.Error("code: ${e.code()}")
                } else NetResult.Error(e.message.toString())
            }
        }
    }

    override suspend fun removeDanFav(vote: Vote, postFav: PostDan): NetResult<VoteDan> {
        return withContext(Dispatchers.IO) {
            try {
                val data = danbooruApi.removeFavPostAsync(DanUrlHelper.getRemoveFavUrl(vote)).await()
                db.postDanDao().deletePost(postFav)
                NetResult.Success(data)
            } catch (e: Exception) {
                if (e is HttpException)
                    NetResult.Error("code: ${e.code()}")
                else
                    NetResult.Error(e.message.toString())
            }
        }
    }

    override suspend fun addDanOneFav(vote: Vote, post: PostDanOne): NetResult<VoteDan> {
        return withContext(Dispatchers.IO) {
            try {
                val data = danbooruOneApi.favPostAsync(
                    url = DanOneUrlHelper.getAddFavUrl(vote),
                    id = vote.post_id,
                    username = vote.username,
                    passwordHash = vote.auth_key
                ).await()
                post.scheme = vote.scheme
                post.keyword = "fav:${vote.username}"
                post.uid = 0L
                db.postDanOneDao().insert(post)
                NetResult.Success(data)
            } catch (e: Exception) {
                if (e is HttpException)
                    NetResult.Error("code: ${e.code()}")
                else
                    NetResult.Error(e.message.toString())
            }
        }
    }

    override suspend fun removeDanOneFav(vote: Vote, postFav: PostDanOne): NetResult<VoteDan> {
        return withContext(Dispatchers.IO) {
            try {
                val data = danbooruOneApi.removeFavPostAsync(
                    url = DanOneUrlHelper.getRemoveFavUrl(vote),
                    postId = vote.post_id,
                    username = vote.username,
                    passwordHash = vote.auth_key
                ).await()
                db.postDanOneDao().deletePost(postFav)
                NetResult.Success(data)
            } catch (e: Exception) {
                if (e is HttpException)
                    NetResult.Error("code: ${e.code()}")
                else
                    NetResult.Error(e.message.toString())
            }
        }
    }

    override suspend fun addSankakuFav(vote: Vote, post: PostSankaku): NetResult<VoteSankaku> {
        return withContext(Dispatchers.IO) {
            try {
                val data = sankakuApi.favPostAsync(
                    url = SankakuUrlHelper.getAddFavUrl(vote),
                    postId = vote.post_id,
                    username = vote.username,
                    passwordHash = vote.auth_key
                ).await()
                if (data.post_id == post.id) {
                    post.scheme = vote.scheme
                    post.keyword = "fav:${vote.username}"
                    post.uid = 0L
                    db.postSankakuDao().insert(post)
                    NetResult.Success(data)
                } else NetResult.Error(data.toString())
            } catch (e: Exception) {
                if (e is HttpException)
                    NetResult.Error("code: ${e.code()}")
                else
                    NetResult.Error(e.message.toString())
            }
        }
    }

    override suspend fun removeSankakuFav(vote: Vote, postFav: PostSankaku): NetResult<VoteSankaku> {
        return withContext(Dispatchers.IO) {
            try {
                val data = sankakuApi.removeFavPostAsync(
                    url = SankakuUrlHelper.getRemoveFavUrl(vote),
                    postId = vote.post_id,
                    username = vote.username,
                    passwordHash = vote.auth_key
                ).await()
                if (data.post_id == postFav.id) {
                    db.postSankakuDao().deletePost(postFav)
                    NetResult.Success(data)
                } else NetResult.Error(data.toString())
            } catch (e: Exception) {
                if (e is HttpException)
                    NetResult.Error("code: ${e.code()}")
                else
                    NetResult.Error(e.message.toString())
            }
        }
    }
}