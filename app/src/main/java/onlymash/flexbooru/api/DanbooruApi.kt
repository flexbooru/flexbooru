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
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import onlymash.flexbooru.common.Constants
import onlymash.flexbooru.common.Constants.BASE_URL
import onlymash.flexbooru.entity.User
import onlymash.flexbooru.entity.VoteDan
import onlymash.flexbooru.entity.artist.ArtistDan
import onlymash.flexbooru.entity.comment.CommentDan
import onlymash.flexbooru.entity.comment.CommentResponse
import onlymash.flexbooru.entity.pool.PoolDan
import onlymash.flexbooru.entity.post.PostDan
import onlymash.flexbooru.entity.tag.TagDan
import onlymash.flexbooru.extension.getUserAgent
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

@Keep
interface DanbooruApi {

    companion object {
        /**
         * return [DanbooruApi]
         * */
        operator fun invoke(): DanbooruApi {

            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { log ->
                Log.d("DanbooruApi", log)
            }).apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val interceptor = Interceptor { chain ->
                val requests =  chain.request().newBuilder()
                    .removeHeader(Constants.USER_AGENT_KEY)
                    .addHeader(Constants.USER_AGENT_KEY, getUserAgent())
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
                .create(DanbooruApi::class.java)
        }
    }

    @GET
    suspend fun getPosts(@Url httpUrl: HttpUrl): Response<MutableList<PostDan>>

    @GET
    suspend fun getUsers(@Url httpUrl: HttpUrl): Response<MutableList<User>>

    @GET
    fun getPools(@Url httpUrl: HttpUrl): Call<MutableList<PoolDan>>

    @GET
    fun getTags(@Url httpUrl: HttpUrl): Call<MutableList<TagDan>>

    @GET
    fun getArtists(@Url httpUrl: HttpUrl): Call<MutableList<ArtistDan>>

    @FormUrlEncoded
    @POST
    fun favPost(@Url url: String,
                @Field("post_id") id: Int,
                @Field("login") username: String,
                @Field("api_key") apiKey: String): Call<VoteDan>

    @DELETE
    fun removeFavPost(@Url httpUrl: HttpUrl): Call<ResponseBody>

    @GET
    fun getComments(@Url httpUrl: HttpUrl): Call<MutableList<CommentDan>>

    @FormUrlEncoded
    @POST
    fun createComment(@Url url: String,
                      @Field("comment[post_id]") postId: Int,
                      @Field("comment[body]") body: String,
                      @Field("comment[do_not_bump_post]") anonymous: Int,
                      @Field("login") username: String,
                      @Field("api_key") apiKey: String): Call<CommentResponse>

    @FormUrlEncoded
    @HTTP(method = "DELETE", hasBody = true)
    fun deleteComment(@Url url: String,
                      @Field("login") username: String,
                      @Field("api_key") apiKey: String): Call<CommentResponse>
}