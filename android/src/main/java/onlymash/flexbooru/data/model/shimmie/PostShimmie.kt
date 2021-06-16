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

package onlymash.flexbooru.data.model.shimmie

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.data.model.common.TagBase
import onlymash.flexbooru.data.model.common.User
import onlymash.flexbooru.data.utils.getShimmieDateMillis
import onlymash.flexbooru.data.utils.toSafeUrl


@Serializable
@SerialName("post")
data class PostShimmie(
    @SerialName("id")
    val id: Int,
    @SerialName("preview_url")
    val previewUrl: String?,
    @SerialName("preview_width")
    val previewWidth: Int,
    @SerialName("preview_height")
    val previewHeight: Int,
    @SerialName("file_url")
    val fileUrl: String?,
    @SerialName("file_name")
    val fileName: String,
    @SerialName("width")
    val width: Int,
    @SerialName("height")
    val height: Int,
    @SerialName("score")
    val score: Int,
    @SerialName("tags")
    val tags: String,
    @SerialName("date")
    val date: String,
    @SerialName("author")
    val author: String,
    @SerialName("rating")
    val rating: String,
    @SerialName("source")
    val source: String? = null
) {
    fun toPost(booruUid: Long, query: String, scheme: String, host: String, index: Int): Post {
        return Post(
            booruUid = booruUid,
            query = query,
            index = index,
            id = id,
            width = width,
            height = height,
            size = 0,
            score = score,
            rating = rating,
            time = date.getShimmieDateMillis(),
            tags = tags.split(" ").map { TagBase(it, -1) },
            preview = previewUrl?.toSafeUrl(scheme, host) ?: "",
            sample = fileUrl?.toSafeUrl(scheme, host) ?: "",
            medium = fileUrl?.toSafeUrl(scheme, host) ?: "",
            origin = fileUrl?.toSafeUrl(scheme, host) ?: "",
            source = source,
            uploader = User(id = 1, name = author),
            isFavored = false
        )
    }
}

@Serializable
@SerialName("tag")
data class PostShimmieTag(
    @SerialName("id")
    val id: Int,
    @SerialName("preview_url")
    val previewUrl: String?,
    @SerialName("preview_width")
    val previewWidth: Int,
    @SerialName("preview_height")
    val previewHeight: Int,
    @SerialName("file_url")
    val fileUrl: String?,
    @SerialName("file_name")
    val fileName: String,
    @SerialName("width")
    val width: Int,
    @SerialName("height")
    val height: Int,
    @SerialName("score")
    val score: Int,
    @SerialName("tags")
    val tags: String,
    @SerialName("date")
    val date: String,
    @SerialName("author")
    val author: String,
    @SerialName("rating")
    val rating: String,
    @SerialName("source")
    val source: String? = null
) {
    fun toPost(booruUid: Long, query: String, scheme: String, host: String, index: Int): Post {
        return Post(
            booruUid = booruUid,
            query = query,
            index = index,
            id = id,
            width = width,
            height = height,
            size = 0,
            score = score,
            rating = rating,
            time = date.getShimmieDateMillis(),
            tags = tags.split(" ").map { TagBase(it, -1) },
            preview = previewUrl?.toSafeUrl(scheme, host) ?: "",
            sample = fileUrl?.toSafeUrl(scheme, host) ?: "",
            medium = fileUrl?.toSafeUrl(scheme, host) ?: "",
            origin = fileUrl?.toSafeUrl(scheme, host) ?: "",
            source = source,
            uploader = User(id = 1, name = author),
            isFavored = false
        )
    }
}