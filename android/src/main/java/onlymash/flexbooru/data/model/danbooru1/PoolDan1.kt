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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.data.model.common.Date
import onlymash.flexbooru.data.model.common.Pool

@Serializable
data class PoolDan1(
    @SerialName("user_id")
    val userId: Int,
    @SerialName("is_public")
    val isPublic: Boolean,
    @SerialName("post_count")
    val postCount: Int,
    @SerialName("name")
    val name: String,
    @SerialName("updated_at")
    val updatedAt: Date,
    @SerialName("id")
    val id: Int,
    @SerialName("created_at")
    val createdAt: Date
) {
    fun toPool(scheme: String, host: String): Pool {
        return Pool(
            booruType = BOORU_TYPE_DAN1,
            scheme = scheme,
            host = host,
            id = id,
            name = name,
            count = postCount,
            time = updatedAt.s * 1000L,
            description = "",
            creatorId = userId
        )
    }
}