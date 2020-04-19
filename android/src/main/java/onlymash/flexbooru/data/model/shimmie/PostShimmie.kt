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

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Xml
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.data.model.common.TagBase
import onlymash.flexbooru.data.model.common.User
import onlymash.flexbooru.data.utils.getShimmieDateMillis
import onlymash.flexbooru.data.utils.toSafeUrl


@Xml(name = "post")
data class PostShimmie(
    @Attribute(name = "id")
    val id: Int,
    @Attribute(name = "preview_url")
    val previewUrl: String?,
    @Attribute(name = "preview_width")
    val previewWidth: Int,
    @Attribute(name = "preview_height")
    val previewHeight: Int,
    @Attribute(name = "file_url")
    val fileUrl: String?,
    @Attribute(name = "file_name")
    val fileName: String,
    @Attribute(name = "width")
    val width: Int,
    @Attribute(name = "height")
    val height: Int,
    @Attribute(name = "score")
    val score: Int,
    @Attribute(name = "tags")
    val tags: String,
    @Attribute(name = "date")
    val date: String,
    @Attribute(name = "author")
    val author: String,
    @Attribute(name = "rating")
    val rating: String,
    @Attribute(name = "")
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