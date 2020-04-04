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
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import onlymash.flexbooru.common.Keys.HEADER_USER_AGENT
import onlymash.flexbooru.common.Values.BASE_URL
import onlymash.flexbooru.data.model.common.BoolResponse
import onlymash.flexbooru.data.model.common.User
import onlymash.flexbooru.data.model.danbooru.*
import onlymash.flexbooru.extension.getUserAgent
import onlymash.flexbooru.util.Logger
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface DanbooruApi {

    companion object {
        val E621_HOSTS = arrayOf("e621.net", "e926.net")
        /**
         * return [DanbooruApi]
         * */
        operator fun invoke(): DanbooruApi {

            val logger = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Logger.d("DanbooruApi", message)
                }
            }).apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val interceptor = Interceptor { chain ->
                val requests =  chain.request().newBuilder()
                    .removeHeader(HEADER_USER_AGENT)
                    .addHeader(HEADER_USER_AGENT, getUserAgent())
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
                .create(DanbooruApi::class.java)
        }
    }

    @GET
    suspend fun getPosts(@Url httpUrl: HttpUrl): Response<List<PostDan>>

    @GET
    suspend fun getPostsE621(@Url httpUrl: HttpUrl): Response<PostDanE621Response>

    @GET
    suspend fun getUsers(@Url httpUrl: HttpUrl): Response<List<User>>

    @GET
    suspend fun getPools(@Url httpUrl: HttpUrl): Response<List<PoolDan>>

    @GET
    suspend fun getTags(@Url httpUrl: HttpUrl): Response<List<TagDan>>

    @GET
    suspend fun getArtists(@Url httpUrl: HttpUrl): Response<List<ArtistDan>>

    @FormUrlEncoded
    @POST
    suspend fun favPost(@Url url: String,
                @Field("post_id") id: Int,
                @Field("login") username: String,
                @Field("api_key") apiKey: String): Response<VoteDan>

    @DELETE
    suspend fun removeFavPost(@Url httpUrl: HttpUrl): Response<ResponseBody>

    @GET
    suspend fun getComments(@Url httpUrl: HttpUrl): Response<List<CommentDan>>

    @FormUrlEncoded
    @POST
    suspend fun createComment(@Url url: String,
                      @Field("comment[post_id]") postId: Int,
                      @Field("comment[body]") body: String,
                      @Field("comment[do_not_bump_post]") anonymous: Int,
                      @Field("login") username: String,
                      @Field("api_key") apiKey: String): Response<BoolResponse>

    @FormUrlEncoded
    @HTTP(method = "DELETE", hasBody = true)
    suspend fun deleteComment(@Url url: String,
                      @Field("login") username: String,
                      @Field("api_key") apiKey: String): Response<BoolResponse>
}