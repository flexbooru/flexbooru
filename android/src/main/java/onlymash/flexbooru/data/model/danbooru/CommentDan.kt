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

package onlymash.flexbooru.data.model.danbooru

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.data.model.common.Comment
import onlymash.flexbooru.data.model.common.User
import onlymash.flexbooru.data.utils.getDanDateMillis

@Serializable
data class CommentDan(
    @SerialName("body")
    val body: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("creator")
    val creator: User? = null,
    @SerialName("id")
    val id: Int,
    @SerialName("post_id")
    val postId: Int,
    @SerialName("creator_id")
    val creatorId: Int? = -1,
    @SerialName("creator_name")
    val creatorName: String? = null
) {
    fun toComment(): Comment {
        return Comment(
            booruType = BOORU_TYPE_DAN,
            id = id,
            postId = postId,
            body = body,
            time = createdAt.getDanDateMillis(),
            creatorId = creator?.id ?: creatorId ?: -1,
            creatorName = creator?.name ?: creatorName ?: ""
        )
    }
}