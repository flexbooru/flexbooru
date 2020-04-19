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

package onlymash.flexbooru.data.model.sankaku

import androidx.room.ColumnInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.data.model.common.Date
import onlymash.flexbooru.data.model.common.TagBase
import onlymash.flexbooru.data.model.common.User
import onlymash.flexbooru.data.utils.toSafeUrl


@Serializable
data class PostSankaku(
    @SerialName("author")
    val author: AuthorSankaku,
    @SerialName("created_at")
    val createdAt: Date,
    @SerialName("fav_count")
    val favCount: Int,
    @SerialName("file_size")
    val fileSize: Int,
    @SerialName("file_url")
    val fileUrl: String,
    @SerialName("has_children")
    val hasChildren: Boolean,
    @SerialName("height")
    val height: Int,
    @SerialName("id")
    val id: Int,
    @SerialName("is_favorited")
    val isFavorited: Boolean,
    @SerialName("parent_id")
    val parentId: Int?,
    @SerialName("preview_height")
    val previewHeight: Int,
    @SerialName("preview_url")
    val previewUrl: String,
    @SerialName("preview_width")
    val previewWidth: Int,
    @SerialName("rating")
    val rating: String?,
    @SerialName("sample_height")
    val sampleHeight: Int,
    @SerialName("sample_url")
    val sampleUrl: String,
    @ColumnInfo(name = "sample_width")
    @SerialName("sample_width")
    val sampleWidth: Int,
    @ColumnInfo(name = "source")
    @SerialName("source")
    val source: String?,
    @SerialName("tags")
    val tags: List<TagSankaku>,
    @SerialName("total_score")
    val totalScore: Int,
    @SerialName("vote_count")
    val voteCount: Int,
    @SerialName("width")
    val width: Int
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

    private fun TagSankaku.toTagBase(): TagBase = TagBase(name, type)

    private fun List<TagSankaku>.toTags() = map { it.toTagBase() }

    fun toPost(booruUid: Long, query: String, scheme: String, host: String, index: Int, isFavored: Boolean): Post {
        return Post(
            booruUid = booruUid,
            query = query,
            index = index,
            id = id,
            width = width,
            height = height,
            size = fileSize,
            score = totalScore,
            rating = rating ?: "e",
            time = createdAt.s * 1000L,
            tags = tags.toTags(),
            preview = previewUrl(scheme, host),
            sample = sampleUrl(scheme, host),
            medium = mediumUrl(scheme, host),
            origin = originUrl(scheme, host),
            source = source,
            isFavored = isFavored,
            uploader = User(id = author.id, name = author.name, avatar = author.avatar)
        )
    }

}