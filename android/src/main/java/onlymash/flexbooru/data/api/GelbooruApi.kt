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
import okhttp3.ResponseBody
import onlymash.flexbooru.common.Keys
import onlymash.flexbooru.data.model.gelbooru.CommentGelResponse
import onlymash.flexbooru.data.model.gelbooru.PostGelResponse
import onlymash.flexbooru.data.model.gelbooru.TagGelResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Url

interface GelbooruApi {

    @GET
    suspend fun getPosts(@Url httpUrl: HttpUrl): Response<PostGelResponse>

    @GET
    suspend fun getTags(@Url httpUrl: HttpUrl): Response<TagGelResponse>

    @GET
    suspend fun getComments(@Url httpUrl: HttpUrl): Response<CommentGelResponse>

    @GET
    suspend fun favPost(
        @Header(Keys.HEADER_COOKIE) cookie: String? = null,
        @Url httpUrl: HttpUrl
    ): Response<ResponseBody>
}