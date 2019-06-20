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

package onlymash.flexbooru.api

import androidx.annotation.Keep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import onlymash.flexbooru.common.HttpHeaders
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.extension.getUserAgent
import onlymash.flexbooru.util.Logger
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

/**
 * App update api
 * */
@Keep
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
                    .removeHeader(HttpHeaders.UserAgent)
                    .addHeader(HttpHeaders.UserAgent, getUserAgent())
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

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
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
                        Settings.latestVersionName = data.version_name
                        Settings.latestVersionCode = data.version_code
                        Settings.isAvailableOnStore = data.is_available_store
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

/**
 * data class for app/update.json
 * */
@Keep
data class UpdateInfo(
    val version_code: Long,
    val version_name: String,
    val url: String,
    var is_available_store: Boolean = true
)