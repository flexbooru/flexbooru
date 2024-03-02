/*
 * Copyright (C) 2020. by onlymash <fiepi.dev@gmail.com>, All rights reserved
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

package onlymash.flexbooru.data.repository.user

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import onlymash.flexbooru.app.Settings
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.app.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.app.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.model.common.User
import onlymash.flexbooru.data.model.sankaku.LoginBody
import onlymash.flexbooru.data.model.sankaku.RefreshTokenBody
import onlymash.flexbooru.extension.NetResult
import java.util.HashMap
import java.util.concurrent.TimeUnit

/**
 *user repo
 * */
class UserRepositoryImpl(private val booruApis: BooruApis) : UserRepository {

    override suspend fun gelLogin(
        username: String,
        password: String,
        booru: Booru
    ): NetResult<User> {
        val url = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegment("index.php")
            .addQueryParameter("page", "account")
            .addQueryParameter("s", "login")
            .addQueryParameter("code", "00")
            .build()
        val cookiesStore = HashMap<String, List<Cookie>>()
        val builder = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .cookieJar(object : CookieJar {
                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    cookiesStore[booru.host] = cookies
                }
                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    return cookiesStore[booru.host] ?: listOf()
                }
            })
//        if (Settings.isSniDisable) {
//            builder.connectionSpecs(NoSniFactory.tls)
//            builder.sslSocketFactory(NoSniFactory, NoSniFactory.defaultTrustManager)
//        }
        if (Settings.isDohEnable) {
            builder.dns(Settings.doh)
        }
        val client = builder.build()
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
                            when (it.name) {
                                "user_id" -> userId = it.value.toInt()
                                "pass_hash" -> passHash = it.value
                            }
                        }
                        if (userId < 0 || passHash.isEmpty()) {
                            NetResult.Error(response.message)
                        } else {
                            val user = User(
                                name = username,
                                id = userId,
                                token = passHash
                            )
                            NetResult.Success(user)
                        }
                    } else {
                        NetResult.Error("Error")
                    }
                } else {
                    NetResult.Error("code: ${response.code}")
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
            BOORU_TYPE_DAN -> findDanUser(username, booru)
            BOORU_TYPE_MOE -> findMoeUser(username, booru)
            BOORU_TYPE_DAN1 -> findDan1User(username, booru)
            BOORU_TYPE_SANKAKU -> findSankakuUser(username, booru)
            else -> NetResult.Error("unknown type")
        }
    }

    override suspend fun findUserById(id: Int, booru: Booru): NetResult<User> {
        return when (booru.type) {
            BOORU_TYPE_MOE -> findMoeUserById(id, booru)
            BOORU_TYPE_DAN1 -> findDan1UserById(id, booru)
            else -> NetResult.Error("unknown type")
        }
    }

    private suspend fun findMoeUser(username: String, booru: Booru): NetResult<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.moeApi.getUsers(booru.getMoeUserUrl(username))
                val users = response.body()
                if (response.isSuccessful && users != null) {
                    val index = users.indexOfFirst { username.equals(other = it.name, ignoreCase = true) }
                    if (index == -1) {
                        NetResult.Error("User not found!")
                    } else {
                        NetResult.Success(users[index])
                    }
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.toString())
            }
        }
    }

    private suspend fun findDanUser(username: String, booru: Booru): NetResult<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.danApi.getUsers(booru.getDanUserUrl(username))
                val users = response.body()
                if (response.isSuccessful && users != null) {
                    val index = users.indexOfFirst { username.equals(other = it.name, ignoreCase = true) }
                    if (index == -1) {
                        NetResult.Error("User not found!")
                    } else {
                        NetResult.Success(users[index])
                    }
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.toString())
            }
        }
    }

    /**
     *search moebooru user by id
     * */
    private suspend fun findMoeUserById(id: Int, booru: Booru): NetResult<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.moeApi.getUsers(booru.getMoeUserUrlById(id))
                val users = response.body()
                if (response.isSuccessful && users != null) {
                    if (users.size == 1) {
                        NetResult.Success(users[0])
                    } else {
                        NetResult.Error("User not found!")
                    }
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.toString())
            }
        }
    }

    private suspend fun findDan1User(username: String, booru: Booru): NetResult<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.dan1Api.getUsers(booru.getDan1UserUrl(username))
                val users = response.body()
                if (response.isSuccessful && users != null) {
                    val index = users.indexOfFirst { username.equals(other = it.name, ignoreCase = true) }
                    if (index == -1) {
                        NetResult.Error("User not found!")
                    } else {
                        NetResult.Success(users[index])
                    }
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.toString())
            }
        }
    }

    private suspend fun findSankakuUser(username: String, booru: Booru): NetResult<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.sankakuApi.getUsers(booru.getSankakuUserUrl(username))
                val users = response.body()
                if (response.isSuccessful && users != null) {
                    val index = users.indexOfFirst { username.equals(other = it.name, ignoreCase = true) }
                    if (index == -1) {
                        NetResult.Error("User not found!")
                    } else {
                        NetResult.Success(users[index])
                    }
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.toString())
            }
        }
    }

    /**
     *search danbooru1.x user by id
     * */
    private suspend fun findDan1UserById(id: Int, booru: Booru): NetResult<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.dan1Api.getUsers(booru.getDan1UserUrlById(id))
                val users = response.body()
                if (response.isSuccessful && users != null) {
                    if (users.size == 1) {
                        NetResult.Success(users[0])
                    } else {
                        NetResult.Error("User not found!")
                    }
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.toString())
            }
        }
    }

    override suspend fun sankakuLogin(
        username: String,
        password: String,
        booru: Booru
    ): NetResult<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.sankakuApi.login(booru.getSankakuTokenUrl(), LoginBody(username, password))
                val sankakuUser = response.body()
                if (response.isSuccessful && sankakuUser != null) {
                    NetResult.Success(sankakuUser.toUser())
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.toString())
            }
        }
    }

    override suspend fun sankakuRefreshToken(refreshToken: String, booru: Booru): NetResult<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.sankakuApi.refreshToken(booru.getSankakuTokenUrl(), RefreshTokenBody(refreshToken))
                val sankakuUser = response.body()
                if (response.isSuccessful && sankakuUser != null) {
                    NetResult.Success(sankakuUser.toUser())
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.toString())
            }
        }
    }

    override suspend fun moeCheck(
        username: String,
        password: String,
        booru: Booru
    ): NetResult<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.moeApi.check(url = booru.getMoeCheckUserUrl(), username = username, password = password)
                val check = response.body()
                if (response.isSuccessful && check != null) {
                    if (check.response != "success") {
                        NetResult.Error(check.response)
                    } else {
                        NetResult.Success(check.toUser())
                    }
                } else {
                    NetResult.Error(response.message())
                }
            } catch (e: Exception) {
                NetResult.Error(e.toString())
            }
        }
    }
}
