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

import okhttp3.HttpUrl
import onlymash.flexbooru.app.Keys
import onlymash.flexbooru.data.model.common.BoolResponse
import onlymash.flexbooru.data.model.common.User
import onlymash.flexbooru.data.model.sankaku.*
import retrofit2.Response
import retrofit2.http.*

interface SankakuApi {

    @GET
    suspend fun getPosts(@Url httpUrl: HttpUrl): Response<ResponseSankaku>

    @GET
    suspend fun getPostsAuth(
        @Url httpUrl: HttpUrl,
        @Header(Keys.HEADER_AUTH) auth: String
    ): Response<ResponseSankaku>

    @GET
    suspend fun getUsers(@Url httpUrl: HttpUrl): Response<List<User>>

    @GET
    suspend fun getPools(@Url httpUrl: HttpUrl): Response<List<PoolSankaku>>

    @GET
    suspend fun getTags(@Url httpUrl: HttpUrl): Response<List<TagSankaku>>

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST
    suspend fun favPost(
        @Url url: HttpUrl,
        @Header(Keys.HEADER_AUTH) auth: String): Response<VoteSankaku>

    @Headers("Content-Type: application/json; charset=utf-8")
    @DELETE
    suspend fun removeFavPost(
        @Url url: HttpUrl,
        @Header(Keys.HEADER_AUTH) auth: String): Response<VoteSankaku>

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET
    suspend fun getPostsComments(
        @Url url: HttpUrl,
        @Header(Keys.HEADER_AUTH) auth: String): Response<List<CommentSankaku>>

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET
    suspend fun getPostComments(
        @Url url: HttpUrl,
        @Header(Keys.HEADER_AUTH) auth: String): Response<List<CommentSankakuItem>>

    /* comment/create.json
     */
    @Headers("Content-Type: application/json; charset=utf-8")
    @POST
    suspend fun createComment(
        @Url url: HttpUrl,
        @Body comment: CommentBody,
        @Header(Keys.HEADER_AUTH) auth: String): Response<BoolResponse>


    /* comment/destroy.json
     */
    @Headers("Content-Type: application/json; charset=utf-8")
    @DELETE
    suspend fun destroyComment(
        @Url url: HttpUrl,
        @Header(Keys.HEADER_AUTH) auth: String): Response<BoolResponse>

    @Headers("Content-Type: application/json")
    @POST
    suspend fun login(
        @Url url: HttpUrl,
        @Body loginBody: LoginBody
    ): Response<UserSankaku>

    @Headers("Content-Type: application/json")
    @POST
    suspend fun refreshToken(
        @Url url: HttpUrl,
        @Body refreshTokenBody: RefreshTokenBody
    ): Response<UserSankaku>
}