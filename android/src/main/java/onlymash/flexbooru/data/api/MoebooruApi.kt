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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import onlymash.flexbooru.common.Keys.HEADER_COOKIE
import onlymash.flexbooru.common.Keys.HEADER_USER_AGENT
import onlymash.flexbooru.common.Values.BASE_URL
import onlymash.flexbooru.common.Settings.activatedBooruUid
import onlymash.flexbooru.data.model.common.Artist
import onlymash.flexbooru.data.model.common.BoolResponse
import onlymash.flexbooru.data.database.CookieManager
import onlymash.flexbooru.data.model.common.User
import onlymash.flexbooru.data.model.moebooru.*
import onlymash.flexbooru.extension.userAgent
import onlymash.flexbooru.util.Logger
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface MoebooruApi {

    companion object {
        /**
         * return [MoebooruApi]
         * */
        operator fun invoke(): MoebooruApi {

            val logger = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Logger.d("MoebooruApi", message)
                }
            }).apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val interceptor = Interceptor { chain ->
                val builder =  chain.request()
                    .newBuilder()
                    .removeHeader(HEADER_USER_AGENT)
                    .addHeader(HEADER_USER_AGENT, userAgent)
                CookieManager.getCookieByBooruUid(activatedBooruUid)?.cookie?.let {
                    builder.addHeader(HEADER_COOKIE, it)
                }

                chain.proceed(builder.build())
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
                .addConverterFactory(Json(JsonConfiguration(
                    ignoreUnknownKeys = true,
                    isLenient = true
                ))
                    .asConverterFactory(contentType))
                .build()
                .create(MoebooruApi::class.java)
        }
    }

    @GET
    suspend fun getPosts(@Url httpUrl: HttpUrl): Response<List<PostMoe>>

    @GET
    suspend fun getUsers(@Url httpUrl: HttpUrl): Response<List<User>>

    @GET
    suspend fun getPools(@Url httpUrl: HttpUrl): Response<List<PoolMoe>>

    @GET
    suspend fun getTags(@Url httpUrl: HttpUrl): Response<List<TagMoe>>

    @GET
    suspend fun getArtists(@Url httpUrl: HttpUrl): Response<List<Artist>>

    @FormUrlEncoded
    @POST
    suspend fun votePost(@Url url: String,
                      @Field("id") id: Int,
                      @Field("score") score: Int = 3, //0-3
                      @Field("login") username: String,
                      @Field("password_hash") passwordHash: String): Response<VoteMoe>

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


    /* user comment/search.json?query=user:username&page=page
     * post comment.json?post_id=post_id
     * all comment/search.json?query=&page=page
     */
    @GET
    suspend fun getComments(@Url url: HttpUrl): Response<List<CommentMoe>>

    /* comment/destroy.json
     */
    @FormUrlEncoded
    @HTTP(method = "DELETE", hasBody = true)
    suspend fun destroyComment(@Url url: String,
                       @Field("id") commentId: Int,
                       @Field("login") username: String,
                       @Field("password_hash") passwordHash: String): Response<BoolResponse>
}