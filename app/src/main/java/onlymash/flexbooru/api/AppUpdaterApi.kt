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

import android.util.Log
import androidx.annotation.Keep
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import onlymash.flexbooru.Constants
import onlymash.flexbooru.Settings
import onlymash.flexbooru.util.UserAgent
import retrofit2.Call
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
        private fun create(): AppUpdaterApi {

            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { log ->
                Log.d("AppUpdaterApi", log)
            }).apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val interceptor = Interceptor { chain ->
                val requests =  chain.request().newBuilder()
                    .removeHeader(Constants.USER_AGENT_KEY)
                    .addHeader(Constants.USER_AGENT_KEY, UserAgent.get())
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
        /**
         * callback [Unit] return [UpdateInfo]
         * */
        fun checkUpdate() {
            create().checkUpdate().enqueue(object : retrofit2.Callback<UpdateInfo> {
                override fun onFailure(call: Call<UpdateInfo>, t: Throwable) {

                }
                override fun onResponse(call: Call<UpdateInfo>, response: Response<UpdateInfo>) {
                    val data = response.body() ?: return
                    Settings.instance().latestVersionUrl = data.url
                    Settings.instance().latestVersionName = data.version_name
                    Settings.instance().latestVersionCode = data.version_code
                    Settings.instance().isAvailableOnStore = data.is_available_store
                }
            })
        }
    }

    /**
     * check app new version
     * */
    @GET("/flexbooru/flexbooru/master/app/update.json")
    fun checkUpdate(): Call<UpdateInfo>
}

/**
 * data class for app/update.json
 * */
@Keep
data class UpdateInfo(
    val version_code: Long,
    val version_name: String,
    val url: String,
    val is_available_store: Boolean
)