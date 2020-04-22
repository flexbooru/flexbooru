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

package onlymash.flexbooru.data.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import onlymash.flexbooru.common.Keys.HEADER_USER_AGENT
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.data.model.app.UpdateInfo
import onlymash.flexbooru.extension.userAgent
import onlymash.flexbooru.util.Logger
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

/**
 * App update api
 * */
interface AppUpdaterApi {

    companion object {
        private const val BASE_URL = "https://raw.githubusercontent.com"
        operator fun invoke(): AppUpdaterApi {

            val logger = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Logger.d("AppUpdaterApi", message)
                }
            }).apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val interceptor = Interceptor { chain ->
                val requests =  chain.request().newBuilder()
                    .removeHeader(HEADER_USER_AGENT)
                    .addHeader(HEADER_USER_AGENT, userAgent)
                    .build()
                chain.proceed(requests)
            }

            val client = OkHttpClient.Builder().apply {
                connectTimeout(10, TimeUnit.SECONDS)
                readTimeout(10, TimeUnit.SECONDS)
                writeTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(interceptor)
                    .addInterceptor(logger)
            }
                .build()

            val contentType = "application/json".toMediaType()
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(Json(JsonConfiguration(ignoreUnknownKeys = true))
                    .asConverterFactory(contentType))
                .build()
                .create(AppUpdaterApi::class.java)
        }

        suspend fun checkUpdate() {
            withContext(Dispatchers.IO) {
                try {
                    val response = AppUpdaterApi().checkUpdate()
                    val data = response.body()
                    if (response.isSuccessful && data != null) {
                        Settings.latestVersionUrl = data.url
                        Settings.latestVersionName = data.versionName
                        Settings.latestVersionCode = data.versionCode
                        Settings.isAvailableOnStore = data.isAvailableStore
                    }
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * check app new version
     * */
    @GET("/flexbooru/flexbooru/master/update.json")
    suspend fun checkUpdate(): Response<UpdateInfo>
}