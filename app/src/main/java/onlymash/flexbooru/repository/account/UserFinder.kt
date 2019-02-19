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

package onlymash.flexbooru.repository.account

import onlymash.flexbooru.Constants
import onlymash.flexbooru.api.ApiUrlHelper
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.entity.User
import retrofit2.Call
import retrofit2.Response

class UserFinder(private val danbooruApi: DanbooruApi,
                 private val moebooruApi: MoebooruApi) {

    private var findUserListener: FindUserListener? = null

    fun setFindUserListener(listener: FindUserListener) {
        findUserListener = listener
    }

    fun findUser(username: String, booru: Booru) {
        when (booru.type) {
            Constants.TYPE_DANBOORU -> findDanUser(username, booru)
            Constants.TYPE_MOEBOORU -> findMoeUser(username, booru)
        }
    }

    private fun findMoeUser(username: String, booru: Booru) {
        moebooruApi.getUsers(ApiUrlHelper.getMoeUserUrl(username, booru))
            .enqueue(object : retrofit2.Callback<MutableList<User>> {
                override fun onFailure(call: Call<MutableList<User>>, t: Throwable) {
                    findUserListener?.onFailed(t.message.toString())
                }
                override fun onResponse(call: Call<MutableList<User>>, response: Response<MutableList<User>>) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        val users = data?: mutableListOf()
                        var user: User? = null
                        users.forEach {
                            if (it.name == username) {
                                user = it
                                return@forEach
                            }
                        }
                        if (user != null) {
                            findUserListener?.onSuccess(user!!)
                        } else {
                            findUserListener?.onFailed("User not found!")
                        }
                    } else {
                        findUserListener?.onFailed("Request failed!")
                    }
                }
            })
    }

    private fun findDanUser(username: String, booru: Booru) {
        danbooruApi.getUsers(ApiUrlHelper.getDanUserUrl(username, booru)).enqueue(object : retrofit2.Callback<MutableList<User>> {
            override fun onFailure(call: Call<MutableList<User>>, t: Throwable) {
                findUserListener?.onFailed(t.message.toString())
            }
            override fun onResponse(call: Call<MutableList<User>>, response: Response<MutableList<User>>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    val users = data?: mutableListOf()
                    var user: User? = null
                    users.forEach {
                        if (it.name == username) {
                            user = it
                            return@forEach
                        }
                    }
                    if (user != null) {
                        findUserListener?.onSuccess(user!!)
                    } else {
                        findUserListener?.onFailed("User not found!")
                    }
                } else {
                    findUserListener?.onFailed("Request failed!")
                }
            }
        })
    }

    fun findMoeUserById(id: Int, booru: Booru) {
        moebooruApi.getUsers(ApiUrlHelper.getMoeUserUrlById(id, booru)).enqueue(object : retrofit2.Callback<MutableList<User>> {
            override fun onFailure(call: Call<MutableList<User>>, t: Throwable) {
                findUserListener?.onFailed(t.message.toString())
            }
            override fun onResponse(call: Call<MutableList<User>>, response: Response<MutableList<User>>) {
                if (response.isSuccessful) {
                    val users = response.body()
                    if (users != null && users.size == 1) {
                        findUserListener?.onSuccess(users[0])
                    } else {
                        findUserListener?.onFailed("User not found!")
                    }
                } else {
                    findUserListener?.onFailed("Request failed!")
                }
            }
        })
    }
}