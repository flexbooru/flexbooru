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
import onlymash.flexbooru.common.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.data.model.common.Comment
import onlymash.flexbooru.extension.formatDate
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class CommentMoe(
    @SerialName("id")
    val id: Int,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("post_id")
    val postId: Int,
    @SerialName("creator")
    val creator: String,
    @SerialName("creator_id")
    val creatorId: Int,
    @SerialName("body")
    val body: String
) {
    private fun date(): CharSequence {
        val date =  when {
            createdAt.contains("T") -> SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.ENGLISH).parse(createdAt)
            createdAt.contains(" ") -> SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(createdAt)
            else -> throw IllegalStateException("Unknown date format: $createdAt")
        } ?: return ""
        return date.time.formatDate()
    }

    fun toComment(): Comment {
        return Comment(
            booruType = BOORU_TYPE_MOE,
            id = id,
            postId = postId,
            body = body,
            date = date().toString(),
            creatorId = creatorId,
            creatorName = creator
        )
    }
}