/*
 * Copyright (C) 2020. by onlymash <fiepi.dev@gmail.com>, All rights reserved
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

import okhttp3.HttpUrl
import okhttp3.ResponseBody
import onlymash.flexbooru.data.model.common.BoolResponse
import onlymash.flexbooru.data.model.common.User
import onlymash.flexbooru.data.model.danbooru.*
import retrofit2.Response
import retrofit2.http.*

interface DanbooruApi {

    companion object {
        val E621_HOSTS = arrayOf("e621.net", "e926.net")
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
    suspend fun favPost(@Url url: HttpUrl,
                @Field("post_id") id: Int,
                @Field("login") username: String,
                @Field("api_key") apiKey: String): Response<ResponseBody>

    @DELETE
    suspend fun removeFavPost(@Url httpUrl: HttpUrl): Response<ResponseBody>

    @GET
    suspend fun getComments(@Url httpUrl: HttpUrl): Response<List<CommentDan>>

    @FormUrlEncoded
    @POST
    suspend fun createComment(@Url url: HttpUrl,
                      @Field("comment[post_id]") postId: Int,
                      @Field("comment[body]") body: String,
                      @Field("comment[do_not_bump_post]") anonymous: Int,
                      @Field("login") username: String,
                      @Field("api_key") apiKey: String): Response<BoolResponse>

    @FormUrlEncoded
    @HTTP(method = "DELETE", hasBody = true)
    suspend fun deleteComment(@Url url: HttpUrl,
                      @Field("login") username: String,
                      @Field("api_key") apiKey: String): Response<BoolResponse>
}