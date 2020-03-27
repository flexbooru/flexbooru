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

package onlymash.flexbooru.data.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import onlymash.flexbooru.common.Keys.HEADER_REFERER
import onlymash.flexbooru.common.Keys.HEADER_USER_AGENT
import onlymash.flexbooru.common.Values.BASE_URL
import onlymash.flexbooru.data.model.common.BoolResponse
import onlymash.flexbooru.data.model.common.User
import onlymash.flexbooru.data.model.sankaku.*
import onlymash.flexbooru.extension.getUserAgent
import onlymash.flexbooru.util.Logger
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface SankakuApi {
    companion object {
        /**
         * return [SankakuApi]
         * */
        operator fun invoke(): SankakuApi {
            val logger = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Logger.d("SankakuApi", message)
                }
            }).apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val interceptor = Interceptor {
                val scheme = it.request().url.scheme
                var host = it.request().url.host
                if (host.startsWith("capi-v2.")) host = host.replaceFirst("capi-v2.", "chan.")
                it.proceed(it.request()
                    .newBuilder()
                    .addHeader("Origin", "$scheme://$host")
                    .addHeader(HEADER_REFERER, "$scheme://$host/post")
                    .removeHeader(HEADER_USER_AGENT)
                    .addHeader(HEADER_USER_AGENT, getUserAgent())
                    .build())
            }
            val client = OkHttpClient.Builder().apply {
                connectTimeout(15, TimeUnit.SECONDS)
                readTimeout(15, TimeUnit.SECONDS)
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
                .create(SankakuApi::class.java)
        }
    }

    @GET
    suspend fun getPosts(@Url httpUrl: HttpUrl): Response<List<PostSankaku>>

    @GET
    suspend fun getUsers(@Url httpUrl: HttpUrl): Response<List<User>>

    @GET
    suspend fun getPools(@Url httpUrl: HttpUrl): Response<List<PoolSankaku>>

    @GET
    suspend fun getTags(@Url httpUrl: HttpUrl): Response<List<TagSankaku>>

    @FormUrlEncoded
    @POST
    suspend fun favPost(@Url url: String,
                     @Field("id") postId: Int,
                     @Field("login") username: String,
                     @Field("password_hash") passwordHash: String): Response<VoteSankaku>

    @FormUrlEncoded
    @HTTP(method = "POST", hasBody = true)
    suspend fun removeFavPost(@Url url: String,
                           @Field("id") postId: Int,
                           @Field("login") username: String,
                           @Field("password_hash") passwordHash: String): Response<VoteSankaku>


    @GET
    suspend fun getComments(@Url url: HttpUrl): Response<List<CommentSankaku>>

    /* comment/create.json
     */
    @POST
    @FormUrlEncoded
    suspend fun createComment(@Url url: String,
                      @Field("comment[post_id]") postId: Int,
                      @Field("comment[body]") body: String,
                      @Field("comment[anonymous]") anonymous: Int,
                      @Field("login") username: String,
                      @Field("password_hash") passwordHash: String): Response<BoolResponse>



    /* comment/destroy.json
     */
    @FormUrlEncoded
    @HTTP(method = "DELETE", hasBody = true)
    suspend fun destroyComment(@Url url: String,
                       @Field("id") commentId: Int,
                       @Field("login") username: String,
                       @Field("password_hash") passwordHash: String): Response<BoolResponse>
}