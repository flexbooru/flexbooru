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

package onlymash.flexbooru.data.model.danbooru1

import androidx.room.ColumnInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.data.model.common.Date
import onlymash.flexbooru.data.model.common.TagBase
import onlymash.flexbooru.data.model.common.User
import onlymash.flexbooru.data.utils.toSafeUrl

@Serializable
data class PostDan1(
    @SerialName("id")
    val id: Int,
    @SerialName("creator_id")
    val creatorId: Int,
    @SerialName("source")
    val source: String?,
    @SerialName("author")
    val author: String,
    @SerialName("score")
    val score: Int,
    @SerialName("has_children")
    val hasChildren: Boolean,
    @SerialName("parent_id")
    val parentId: Int?,
    @SerialName("preview_url")
    val previewUrl: String,
    @SerialName("sample_url")
    val sampleUrl: String?,
    @ColumnInfo(name = "file_url")
    @SerialName("file_url")
    val fileUrl: String?,
    @SerialName("sample_width")
    val sampleWidth: Int,
    @SerialName("sample_height")
    val sampleHeight: Int,
    @SerialName("width")
    val width: Int,
    @SerialName("height")
    val height: Int,
    @SerialName("tags")
    val tags: String,
    @SerialName("rating")
    val rating: String,
    @SerialName("file_size")
    val fileSize: Int,
    @SerialName("created_at")
    val createdAt: Date
) {
    private fun previewUrl(scheme: String, host: String) =
        previewUrl.toSafeUrl(scheme, host)

    private fun sampleUrl(scheme: String, host: String) =
        sampleUrl?.toSafeUrl(scheme, host) ?: previewUrl(scheme, host)

    private fun mediumUrl(scheme: String, host: String) = sampleUrl(scheme, host)

    private fun originUrl(scheme: String, host: String) =
        fileUrl?.toSafeUrl(scheme, host) ?: sampleUrl(scheme, host)

    private fun getTags(): List<TagBase> =
        tags.trim().split(" ").map { TagBase(it, -1) }

    fun toPost(booruUid: Long, query: String, scheme: String, host: String, index: Int, isFavored: Boolean): Post {
        return Post(
            booruUid = booruUid,
            query = query,
            index = index,
            id = id,
            width = width,
            height = height,
            size = fileSize,
            score = score,
            rating = rating,
            time = createdAt.s * 1000L,
            tags = getTags(),
            preview = previewUrl(scheme, host),
            sample = sampleUrl(scheme, host),
            medium = mediumUrl(scheme, host),
            origin = originUrl(scheme, host),
            source = source,
            uploader = User(id = creatorId, name = author),
            isFavored = isFavored
        )
    }
}