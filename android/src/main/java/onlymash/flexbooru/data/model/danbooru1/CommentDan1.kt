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

package onlymash.flexbooru.data.model.danbooru1

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.common.Values.DATE_PATTERN
import onlymash.flexbooru.data.model.common.Comment
import onlymash.flexbooru.extension.parseDate

@Serializable
data class CommentDan1(
    @SerialName("id")
    val id: Int,
    @SerialName("score")
    val score: Int,
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
    fun toComment(): Comment {
        return Comment(
            booruType = BOORU_TYPE_DAN1,
            id = id,
            postId = postId,
            body = body,
            time = createdAt.parseDate(DATE_PATTERN),
            creatorId = creatorId,
            creatorName = creator
        )
    }
}