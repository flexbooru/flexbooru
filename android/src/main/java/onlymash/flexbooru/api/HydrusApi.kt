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

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import onlymash.flexbooru.common.Constants.BASE_URL
import onlymash.flexbooru.common.HttpHeaders
import onlymash.flexbooru.entity.post.HydrusAuth
import onlymash.flexbooru.entity.post.PostHydrusFileId
import onlymash.flexbooru.extension.getUserAgent
import onlymash.flexbooru.util.Logger
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

interface HydrusApi {

    companion object {
        /**
         * return [HydrusApi]
         * */
        operator fun invoke(): HydrusApi {

            val logger = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Logger.d("Hydrus", message)
                }
            }).apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val interceptor = Interceptor { chain ->
                val requests = chain.request().newBuilder()
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
                .create(HydrusApi::class.java)
        }
    }

    @GET
    suspend fun getPosts(@Url httpUrl: HttpUrl): Response<PostHydrusFileId>


    @GET
    suspend fun getUsers(@Url httpUrl: HttpUrl): Response<HydrusAuth>

}