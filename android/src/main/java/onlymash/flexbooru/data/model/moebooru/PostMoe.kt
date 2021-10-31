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

package onlymash.flexbooru.data.model.moebooru

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.app.Values
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.data.model.common.TagBase
import onlymash.flexbooru.data.model.common.User
import onlymash.flexbooru.data.utils.toSafeUrl

@Serializable
data class PostMoe(
    @SerialName("id")
    val id: Int,
    @SerialName("tags")
    val tags: String?,
    @SerialName("created_at")
    val createdAt: Int,
    @SerialName("creator_id")
    val creatorId: Int? = -1,
    @SerialName("author")
    val author: String,
    @SerialName("source")
    val source: String?,
    @SerialName("score")
    val score: Int,
    @SerialName("file_size")
    val fileSize: Int?,
    @SerialName("file_url")
    val fileUrl: String? = null,
    @SerialName("preview_url")
    val previewUrl: String,
    @SerialName("preview_width")
    val previewWidth: Int,
    @SerialName("preview_height")
    val previewHeight: Int,
    @SerialName("sample_url")
    val sampleUrl: String? = null,
    @SerialName("sample_width")
    val sampleWidth: Int,
    @SerialName("sample_height")
    val sampleHeight: Int,
    @SerialName("sample_file_size")
    val sampleFileSize: Int?,
    @SerialName("jpeg_url")
    val jpegUrl: String? = null,
    @SerialName("jpeg_width")
    val jpegWidth: Int?,
    @SerialName("jpeg_height")
    val jpegHeight: Int?,
    @SerialName("jpeg_file_size")
    val jpegFileSize: Int?,
    @SerialName("rating")
    val rating: String,
    @SerialName("has_children")
    val hasChildren: Boolean,
    @SerialName("parent_id")
    val parentId: Int?,
    @SerialName("status")
    val status: String,
    @SerialName("width")
    val width: Int,
    @SerialName("height")
    val height: Int
) {
    private fun previewUrl(scheme: String, host: String) =
        previewUrl.toSafeUrl(scheme, host)

    private fun sampleUrl(scheme: String, host: String) =
        sampleUrl?.toSafeUrl(scheme, host) ?: previewUrl(scheme, host)

    private fun mediumUrl(scheme: String, host: String) =
        jpegUrl?.toSafeUrl(scheme, host) ?: sampleUrl(scheme, host)

    private fun originUrl(scheme: String, host: String) =
        fileUrl?.toSafeUrl(scheme, host) ?: sampleUrl(scheme, host)

    private fun String.getTags(tagTypes: Map<String, String>): List<TagBase> =
        trim().split(" ").map { name ->
            val type = when(tagTypes[name]) {
                "general" -> Values.Tags.TYPE_GENERAL
                "artist" -> Values.Tags.TYPE_ARTIST
                "copyright" -> Values.Tags.TYPE_COPYRIGHT
                "character" -> Values.Tags.TYPE_CHARACTER
                "circle" -> Values.Tags.TYPE_CIRCLE
                "faults" -> Values.Tags.TYPE_FAULTS
                else -> Values.Tags.TYPE_ALL
            }
            TagBase(name, type)
        }

    private fun isFavored(votes: Map<String, Int>): Boolean {
        return votes[id.toString()] == 3
    }

    fun toPost(booruUid: Long, query: String, scheme: String, host: String, index: Int, tagTypes: Map<String, String>, votes: Map<String, Int>): Post {
        return Post(
            booruUid = booruUid,
            query = query,
            index = index,
            id = id,
            width = width,
            height = height,
            size = fileSize ?: 0,
            score = score,
            rating = rating,
            time = createdAt * 1000L,
            tags = tags?.getTags(tagTypes) ?: listOf(),
            preview = previewUrl(scheme, host),
            sample = sampleUrl(scheme, host),
            medium = mediumUrl(scheme, host),
            origin = originUrl(scheme, host),
            source = source,
            uploader = User(id = creatorId ?: -1, name = author),
            isFavored = isFavored(votes)
        )
    }
}