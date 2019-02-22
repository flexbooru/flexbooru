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

package onlymash.flexbooru.repository.favorite

import android.util.Log
import onlymash.flexbooru.api.ApiUrlHelper
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.entity.PostDan
import onlymash.flexbooru.entity.Vote
import onlymash.flexbooru.entity.VoteDan
import onlymash.flexbooru.entity.VoteMoe
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.util.concurrent.Executor

class VoteData(private val danbooruApi: DanbooruApi,
               private val moebooruApi: MoebooruApi,
               private val db: FlexbooruDatabase,
               private val ioExecutor: Executor): VoteRepository {

    companion object {
        private const val TAG = "VoteData"
    }

    override fun voteMoePost(vote: Vote) {
        moebooruApi.votePost(
            url = ApiUrlHelper.getMoeVoteUrl(vote),
            id = vote.post_id,
            score = vote.score,
            username = vote.username,
            passwordHash = vote.auth_key)
            .enqueue(object : retrofit2.Callback<VoteMoe> {
                override fun onFailure(call: Call<VoteMoe>, t: Throwable) {
                    onFailed(t.message.toString())
                }
                override fun onResponse(call: Call<VoteMoe>, response: Response<VoteMoe>) {
                    if (response.isSuccessful) {
                        val voteMoe = response.body()
                        if (voteMoe != null && voteMoe.success) {
                            if (vote.score == 3) {
                                ioExecutor.execute {
                                    val post= voteMoe.posts[0].apply {
                                        host = vote.host
                                        keyword = "vote:3:${vote.username} order:vote"
                                    }
                                    db.postMoeDao().insert(post)
                                }
                            } else {
                                ioExecutor.execute {
                                    db.postMoeDao().deletePost(
                                        host = vote.host,
                                        keyword = "vote:3:${vote.username} order:vote",
                                        id = vote.post_id
                                    )
                                }
                            }
                            onSuccess()
                        } else {
                            onFailed("Unknown issue")
                        }
                    } else {
                        onFailed(response.message())
                    }
                }
            })
    }

    override fun addDanFav(vote: Vote, post: PostDan) {
        danbooruApi.favPost(
            url = ApiUrlHelper.getDanAddFavUrl(vote),
            id = vote.post_id,
            username = vote.username,
            apiKey = vote.auth_key
        ).enqueue(object : Callback<VoteDan> {
            override fun onFailure(call: Call<VoteDan>, t: Throwable) {
                if (t is HttpException) {
                    val data = t.response().errorBody()
                    if (t.code() == 500){
                        onFailed(data.toString())
                    } else {
                        onFailed(t.message())
                    }
                } else {
                    onFailed(t.message.toString())
                }
            }
            override fun onResponse(call: Call<VoteDan>, response: Response<VoteDan>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data is VoteDan) {
                        post.keyword = "fav:${vote.username}"
                        post.uid = -1L
                        ioExecutor.execute {
                            db.postDanDao().insert(post)
                        }
                    }
                } else {
                    onFailed(response.message())
                }
            }
        })
    }

    override fun removeDanFav(vote: Vote, postFav: PostDan) {
        danbooruApi.removeFavPost(ApiUrlHelper.getDanRemoveFavUrl(vote))
            .enqueue(object : Callback<VoteDan> {
                override fun onFailure(call: Call<VoteDan>, t: Throwable) {
                    onFailed(t.message.toString())
                }
                override fun onResponse(call: Call<VoteDan>, response: Response<VoteDan>) {
                    if (response.isSuccessful) {
                        ioExecutor.execute {
                            db.postDanDao().deletePost(postFav)
                        }
                        onSuccess()
                    } else {
                        onFailed(response.message())
                    }
                }
            })
    }

    override fun onSuccess() {

    }

    override fun onFailed(msg: String) {
        Log.w(TAG, msg)
    }
}