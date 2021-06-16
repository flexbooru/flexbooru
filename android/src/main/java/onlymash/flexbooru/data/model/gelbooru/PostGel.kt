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

package onlymash.flexbooru.data.model.gelbooru

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.data.model.common.TagBase
import onlymash.flexbooru.data.model.common.User
import onlymash.flexbooru.data.utils.getGelDateMillis
import onlymash.flexbooru.data.utils.toSafeUrl

@Serializable
@SerialName("post")
data class PostGel(
    @SerialName("id")
    val id: Int,
    @SerialName("width")
    val width: Int,
    @SerialName("height")
    val height: Int,
    @SerialName("score")
    val score: String,
    @SerialName("file_url")
    val fileUrl: String,
    @SerialName("sample_url")
    val sampleUrl: String,
    @SerialName("sample_width")
    val sampleWidth: Int,
    @SerialName("sample_height")
    val sampleHeight: Int,
    @SerialName("preview_url")
    val previewUrl: String,
    @SerialName("rating")
    val rating: String,
    @SerialName("tags")
    val tags: String,
    @SerialName("creator_id")
    val creatorId: Int,
    @SerialName("has_children")
    val hasChildren: Boolean,
    @SerialName("created_at")
    val createdAt:	String,
    @SerialName("source")
    val source: String,
    @SerialName("preview_width")
    val previewWidth: Int,
    @SerialName("preview_height")
    val previewHeight: Int
) {
    private fun previewUrl(scheme: String, host: String) =
        previewUrl.toSafeUrl(scheme, host)

    private fun sampleUrl(scheme: String, host: String) =
        sampleUrl.toSafeUrl(scheme, host)

    private fun mediumUrl(scheme: String, host: String) = sampleUrl(scheme, host)

    private fun originUrl(scheme: String, host: String) =
        fileUrl.toSafeUrl(scheme, host)

    private fun String.getTags(): List<TagBase> =
        trim().split(" ").map { TagBase(it, -1) }

    private fun getIntScore(): Int = if (score.isEmpty()) 0 else score.trim().toInt()

    fun toPost(booruUid: Long, query: String, scheme: String, host: String, index: Int): Post {
        return Post(
            booruUid = booruUid,
            query = query,
            index = index,
            id = id,
            width = width,
            height = height,
            size = 0,
            score = getIntScore(),
            rating = rating,
            time = createdAt.getGelDateMillis(),
            tags = tags.getTags(),
            preview = previewUrl(scheme, host),
            sample = sampleUrl(scheme, host),
            medium = mediumUrl(scheme, host),
            origin = originUrl(scheme, host),
            source = source,
            uploader = User(id = creatorId, name = "")
        )
    }
}