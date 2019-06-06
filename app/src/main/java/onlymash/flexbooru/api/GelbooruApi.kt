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
import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import onlymash.flexbooru.common.Constants
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.database.CookieManager
import onlymash.flexbooru.entity.comment.CommentGelResponse
import onlymash.flexbooru.entity.post.PostGelResponse
import onlymash.flexbooru.entity.tag.TagGelResponse
import onlymash.flexbooru.extension.getUserAgent
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

@Keep
interface GelbooruApi {
    companion object {
        /**
         * return [GelbooruApi]
         * */
        operator fun invoke(): GelbooruApi {

            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { log ->
                Log.d("GelbooruApi", log)
            }).apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val interceptor = Interceptor { chain ->
                val builder =  chain.request().newBuilder()
                    .removeHeader(Constants.USER_AGENT_KEY)
                    .addHeader(Constants.USER_AGENT_KEY, getUserAgent())
                val cookie = CookieManager.getCookieByBooruUid(Settings.activeBooruUid)?.cookie
                if (!cookie.isNullOrEmpty()) {
                    builder.addHeader("cookie", cookie)
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

            return Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(
                    TikXmlConverterFactory.create(
                        TikXml.Builder()
                            .exceptionOnUnreadXml(false)
                            .build()
                    )
                )
                .build()
                .create(GelbooruApi::class.java)
        }
    }

    @GET
    suspend fun getPosts(@Url httpUrl: HttpUrl): Response<PostGelResponse>

    @GET
    fun getTags(@Url httpUrl: HttpUrl): Call<TagGelResponse>

    @GET
    fun getComments(@Url httpUrl: HttpUrl): Call<CommentGelResponse>

    @GET
    suspend fun favPost(@Url httpUrl: HttpUrl): Response<ResponseBody>
}