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

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import onlymash.flexbooru.Constants
import onlymash.flexbooru.api.url.MoeUrlHelper
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.DanbooruOneApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.api.SankakuApi
import onlymash.flexbooru.api.url.DanOneUrlHelper
import onlymash.flexbooru.api.url.DanUrlHelper
import onlymash.flexbooru.api.url.SankakuUrlHelper
import onlymash.flexbooru.database.CookieManager
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.entity.User
import onlymash.flexbooru.extension.NetResult
import java.util.HashMap
import java.util.concurrent.TimeUnit

/**
 *user repo
 * */
class UserRepositoryImpl(private val danbooruApi: DanbooruApi,
                         private val danbooruOneApi: DanbooruOneApi,
                         private val moebooruApi: MoebooruApi,
                         private val sankakuApi: SankakuApi) : UserRepository {

    override suspend fun gelLogin(
        username: String,
        password: String,
        booru: Booru): NetResult<User> {
        val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { log ->
            Log.d("GelbooruLogin", log)
        }).apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val url = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("index.php")
            .addQueryParameter("page", "account")
            .addQueryParameter("s", "login")
            .addQueryParameter("code", "00")
            .build()
        val cookiesStore = HashMap<String, List<Cookie>>()
        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .cookieJar(object : CookieJar {
                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    cookiesStore[booru.host] = cookies
                }
                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    return cookiesStore[booru.host] ?: listOf()
                }
            })
            .addInterceptor(logger)
            .build()
        val formBody = FormBody.Builder()
            .add("user", username)
            .add("pass", password)
            .add("submit","Log in")
            .build()
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()
        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val cookies = cookiesStore[booru.host]
                    if (cookies != null) {
                        var userId = -1
                        var passHash = ""
                        cookies.forEach {
                            when (it.name()) {
                                "user_id" -> userId = it.value().toInt()
                                "pass_hash" -> passHash = it.value()
                            }
                        }
                        if (userId < 0 || passHash.isEmpty()) {
                            NetResult.Error(response.message())
                        } else {
                            val user = User(
                                booru_uid = booru.uid,
                                name = username,
                                id = userId,
                                password_hash = passHash
                            )
                            CookieManager.createCookie(onlymash.flexbooru.entity.Cookie(
                                booru_uid = booru.uid,
                                cookie = "user_id=$userId; pass_hash=$passHash"
                            ))
                            NetResult.Success(user)
                        }
                    } else {
                        NetResult.Error("Error")
                    }
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (ex: Exception) {
                NetResult.Error(ex.message.toString())
            }
        }
    }

    /**
     *search user
     * */
    override suspend fun findUserByName(username: String, booru: Booru): NetResult<User> {
        return when (booru.type) {
            Constants.TYPE_DANBOORU -> findDanUser(username, booru)
            Constants.TYPE_MOEBOORU -> findMoeUser(username, booru)
            Constants.TYPE_DANBOORU_ONE -> findDanOneUser(username, booru)
            Constants.TYPE_SANKAKU -> findSankakuUser(username, booru)
            else -> NetResult.Error("unknown type")
        }
    }

    override suspend fun findUserById(id: Int, booru: Booru): NetResult<User> {
        return when (booru.type) {
            Constants.TYPE_MOEBOORU -> findMoeUserById(id, booru)
            Constants.TYPE_DANBOORU_ONE -> findDanOneUserById(id, booru)
            else -> NetResult.Error("unknown type")
        }
    }

    private suspend fun findMoeUser(username: String, booru: Booru): NetResult<User> {
        return try {
            val users = moebooruApi.getUsersAsync(MoeUrlHelper.getUserUrl(username, booru)).await()
            val index = users.indexOfFirst { username.equals(other = it.name, ignoreCase = true) }
            if (index == -1) {
                NetResult.Error("User not found!")
            } else {
                NetResult.Success(users[index])
            }
        } catch (e: Exception) {
            NetResult.Error(e.toString())
        }
    }

    private suspend fun findDanUser(username: String, booru: Booru): NetResult<User> {
        return try {
            val users = danbooruApi.getUsersAsync(DanUrlHelper.getUserUrl(username, booru)).await()
            val index = users.indexOfFirst { username.equals(other = it.name, ignoreCase = true) }
            if (index == -1) {
                NetResult.Error("User not found!")
            } else {
                NetResult.Success(users[index])
            }
        } catch (e: Exception) {
            NetResult.Error(e.toString())
        }
    }

    /**
     *search moebooru user by id
     * */
    private suspend fun findMoeUserById(id: Int, booru: Booru): NetResult<User> {
        return try {
            val users = moebooruApi.getUsersAsync(MoeUrlHelper.getUserUrlById(id, booru)).await()
            if (users.size == 1) {
                NetResult.Success(users[0])
            } else {
                NetResult.Error("User not found!")
            }
        } catch (e: Exception) {
            NetResult.Error(e.toString())
        }
    }

    private suspend fun findDanOneUser(username: String, booru: Booru): NetResult<User> {
        return try {
            val users = danbooruOneApi.getUsersAsync(DanOneUrlHelper.getUserUrl(username, booru)).await()
            val index = users.indexOfFirst { username.equals(other = it.name, ignoreCase = true) }
            if (index == -1) {
                NetResult.Error("User not found!")
            } else {
                NetResult.Success(users[index])
            }
        } catch (e: Exception) {
            NetResult.Error(e.toString())
        }
    }

    private suspend fun findSankakuUser(username: String, booru: Booru): NetResult<User> {
        return try {
            val users = sankakuApi.getUsersAsync(SankakuUrlHelper.getUserUrl(username, booru)).await()
            val index = users.indexOfFirst { username.equals(other = it.name, ignoreCase = true) }
            if (index == -1) {
                NetResult.Error("User not found!")
            } else {
                NetResult.Success(users[index])
            }
        } catch (e: Exception) {
            NetResult.Error(e.toString())
        }
    }

    /**
     *search danbooru1.x user by id
     * */
    private suspend fun findDanOneUserById(id: Int, booru: Booru): NetResult<User> {
        return try {
            val users = danbooruOneApi.getUsersAsync(DanOneUrlHelper.getUserUrlById(id, booru)).await()
            if (users.size == 1) {
                NetResult.Success(users[0])
            } else {
                NetResult.Error("User not found!")
            }
        } catch (e: Exception) {
            NetResult.Error(e.toString())
        }
    }
}