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
import okhttp3.logging.HttpLoggingInterceptor
import onlymash.flexbooru.Constants
import onlymash.flexbooru.Settings
import onlymash.flexbooru.database.CookieManager
import onlymash.flexbooru.entity.User
import onlymash.flexbooru.entity.VoteMoe
import onlymash.flexbooru.entity.artist.ArtistMoe
import onlymash.flexbooru.entity.comment.CommentMoe
import onlymash.flexbooru.entity.comment.CommentResponse
import onlymash.flexbooru.entity.pool.PoolMoe
import onlymash.flexbooru.entity.post.PostMoe
import onlymash.flexbooru.entity.tag.TagMoe
import onlymash.flexbooru.util.UserAgent
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

@Keep
interface MoebooruApi {

    companion object {
        /**
         * return [MoebooruApi]
         * */
        fun create(): MoebooruApi {

            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { log ->
                Log.d("MoebooruApi", log)
            }).apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val interceptor = Interceptor { chain ->
                val builder =  chain.request()
                    .newBuilder()
                    .removeHeader(Constants.USER_AGENT_KEY)
                    .addHeader(Constants.USER_AGENT_KEY, UserAgent.get())
                CookieManager.getCookieByBooruUid(Settings.instance().activeBooruUid)?.cookie?.let {
                    builder.addHeader("Cookie", it)
                }

                chain.proceed(builder.build())
            }

            val client = OkHttpClient.Builder().apply {
                connectTimeout(10, TimeUnit.SECONDS)
                readTimeout(10, TimeUnit.SECONDS)
                writeTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(interceptor)
                    .addInterceptor(logger)
                    .addInterceptor(CloudflareInterceptor())
            }
                .build()

            return Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MoebooruApi::class.java)
        }
    }

    @GET
    fun getPosts(@Url httpUrl: HttpUrl): Call<MutableList<PostMoe>>

    @GET
    fun getUsers(@Url httpUrl: HttpUrl): Call<MutableList<User>>

    @GET
    fun getPools(@Url httpUrl: HttpUrl): Call<MutableList<PoolMoe>>

    @GET
    fun getTags(@Url httpUrl: HttpUrl): Call<MutableList<TagMoe>>

    @GET
    fun getArtists(@Url httpUrl: HttpUrl): Call<MutableList<ArtistMoe>>

    @FormUrlEncoded
    @POST
    fun votePost(@Url url: String,
                 @Field("id") id: Int,
                 @Field("score") score: Int = 3, //0-3
                 @Field("login") username: String,
                 @Field("password_hash") passwordHash: String): Call<VoteMoe>

    /* comment/create.json
     */
    @POST
    @FormUrlEncoded
    fun createComment(@Url url: String,
                      @Field("comment[post_id]") postId: Int,
                      @Field("comment[body]") body: String,
                      @Field("comment[anonymous]") anonymous: Int,
                      @Field("login") username: String,
                      @Field("password_hash") passwordHash: String): Call<CommentResponse>


    /* user comment/search.json?query=user:username&page=page
     * post comment.json?post_id=post_id
     * all comment/search.json?query=&page=page
     */
    @GET
    fun getComments(@Url url: HttpUrl): Call<MutableList<CommentMoe>>

    /* comment/destroy.json
     */
    @FormUrlEncoded
    @HTTP(method = "DELETE", hasBody = true)
    fun destroyComment(@Url url: String,
                       @Field("id") commentId: Int,
                       @Field("login") username: String,
                       @Field("password_hash") passwordHash: String): Call<CommentResponse>
}