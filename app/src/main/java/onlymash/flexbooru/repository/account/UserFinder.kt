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

package onlymash.flexbooru.repository.account

import onlymash.flexbooru.Constants
import onlymash.flexbooru.api.url.MoeUrlHelper
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.DanbooruOneApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.api.SankakuApi
import onlymash.flexbooru.api.url.DanOneUrlHelper
import onlymash.flexbooru.api.url.DanUrlHelper
import onlymash.flexbooru.api.url.SankakuUrlHelper
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.entity.User
import retrofit2.Call
import retrofit2.Response

/**
 *user repo
 * */
class UserFinder(private val danbooruApi: DanbooruApi,
                 private val danbooruOneApi: DanbooruOneApi,
                 private val moebooruApi: MoebooruApi,
                 private val sankakuApi: SankakuApi) : UserRepository {

    override var findUserListener: FindUserListener? = null

    /**
     *search user
     * */
    override fun findUserByName(username: String, booru: Booru) {
        when (booru.type) {
            Constants.TYPE_DANBOORU -> findDanUser(username, booru)
            Constants.TYPE_MOEBOORU -> findMoeUser(username, booru)
            Constants.TYPE_DANBOORU_ONE -> findDanOneUser(username, booru)
            Constants.TYPE_SANKAKU -> findSankakuUser(username, booru)
        }
    }

    override fun findUserById(id: Int, booru: Booru) {
        when (booru.type) {
            Constants.TYPE_MOEBOORU -> findMoeUserById(id, booru)
            Constants.TYPE_DANBOORU_ONE -> findDanOneUserById(id, booru)
        }
    }

    private fun findMoeUser(username: String, booru: Booru) {
        moebooruApi.getUsers(MoeUrlHelper.getUserUrl(username, booru))
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
        danbooruApi.getUsers(DanUrlHelper.getUserUrl(username, booru)).enqueue(object : retrofit2.Callback<MutableList<User>> {
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

    /**
     *search moebooru user by id
     * */
    private fun findMoeUserById(id: Int, booru: Booru) {
        moebooruApi.getUsers(MoeUrlHelper.getUserUrlById(id, booru)).enqueue(object : retrofit2.Callback<MutableList<User>> {
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

    private fun findDanOneUser(username: String, booru: Booru) {
        danbooruOneApi.getUsers(DanOneUrlHelper.getUserUrl(username, booru))
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

    private fun findSankakuUser(username: String, booru: Booru) {
        sankakuApi.getUsers(SankakuUrlHelper.getUserUrl(username, booru))
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

    /**
     *search danbooru1.x user by id
     * */
    private fun findDanOneUserById(id: Int, booru: Booru) {
        danbooruOneApi.getUsers(DanOneUrlHelper.getUserUrlById(id, booru)).enqueue(object : retrofit2.Callback<MutableList<User>> {
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