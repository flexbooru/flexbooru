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

package onlymash.flexbooru.data.model.gelbooru

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Xml
import onlymash.flexbooru.data.utils.formatDateGel
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.data.model.common.TagBase
import onlymash.flexbooru.data.model.common.User
import onlymash.flexbooru.data.utils.toSafeUrl

@Xml(name = "post")
data class PostGel(
    @Attribute(name = "id")
    val id: Int,
    @Attribute(name = "width")
    val width: Int,
    @Attribute(name = "height")
    val height: Int,
    @Attribute(name = "score")
    val score: String,
    @Attribute(name = "file_url")
    val fileUrl: String,
    @Attribute(name = "sample_url")
    val sampleUrl: String,
    @Attribute(name = "sample_width")
    val sampleWidth: Int,
    @Attribute(name = "sample_height")
    val sampleHeight: Int,
    @Attribute(name = "preview_url")
    val previewUrl: String,
    @Attribute(name = "rating")
    val rating: String,
    @Attribute(name = "tags")
    val tags: String,
    @Attribute(name = "creator_id")
    val creatorId: Int,
    @Attribute(name = "has_children")
    val hasChildren: Boolean,
    @Attribute(name = "created_at")
    val createdAt:	String,
    @Attribute(name = "source")
    val source: String,
    @Attribute(name = "preview_width")
    val previewWidth: Int,
    @Attribute(name = "preview_height")
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
            date = createdAt.formatDateGel().toString(),
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