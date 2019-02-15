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

import android.os.Handler
import onlymash.flexbooru.Constants
import onlymash.flexbooru.api.ApiUrlHelper
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.entity.User
import java.io.IOException
import java.util.concurrent.Executor

class UserFinder(private val danbooruApi: DanbooruApi,
                 private val moebooruApi: MoebooruApi,
                 private val ioExecutor: Executor) {

    private var findUserListener: FindUserListener? = null

    fun setFindUserListener(listener: FindUserListener) {
        findUserListener = listener
    }

    private val uiHandler = Handler()

    fun findUser(username: String, booru: Booru) {
        when (booru.type) {
            Constants.TYPE_DANBOORU -> findDanUser(username, booru)
            Constants.TYPE_MOEBOORU -> findMoeUser(username, booru)
        }
    }

    private fun findMoeUser(username: String, booru: Booru) {
        ioExecutor.execute {
            var msg = ""
            val request = moebooruApi.getUsers(ApiUrlHelper.getMoeUserUrl(username, booru))
            try {
                val response = request.execute()
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
                    uiHandler.post {
                        findUserListener?.onSuccess(user!!)
                    }
                } else {
                    msg = "User not found!"
                }
            } catch (ioException: IOException) {
                msg = ioException.message ?: "unknown error"
            } finally {
                if (msg.isNotEmpty()) {
                    uiHandler.post {
                        findUserListener?.onFailed(msg)
                    }
                }
            }
        }
    }

    private fun findDanUser(username: String, booru: Booru) {
        ioExecutor.execute {
            var msg = ""
            val request = danbooruApi.getUsers(ApiUrlHelper.getDanUserUrl(username, booru))
            try {
                val response = request.execute()
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
                    uiHandler.post {
                        findUserListener?.onSuccess(user!!)
                    }
                } else {
                    msg = "User not found!"
                }
            } catch (ioException: IOException) {
                msg = ioException.message ?: "unknown error"
            } finally {
                if (msg.isNotEmpty()) {
                    uiHandler.post {
                        findUserListener?.onFailed(msg)
                    }
                }
            }
        }
    }

    fun findMoeUserById(id: Int, booru: Booru) {
        ioExecutor.execute {
            var msg = ""
            val request = moebooruApi.getUsers(ApiUrlHelper.getMoeUserUrlById(id, booru))
            try {
                val response = request.execute()
                val users = response.body()
                if (users != null && users.size == 1) {
                    uiHandler.post {
                        findUserListener?.onSuccess(users[0])
                    }
                } else {
                    msg = "User not found!"
                }
            } catch (ioException: IOException) {
                msg = ioException.message ?: "unknown error"
            } finally {
                if (msg.isNotEmpty()) {
                    uiHandler.post {
                        findUserListener?.onFailed(msg)
                    }
                }
            }
        }
    }
}