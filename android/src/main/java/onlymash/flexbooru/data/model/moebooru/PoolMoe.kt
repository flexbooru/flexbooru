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
import onlymash.flexbooru.data.model.common.Pool
import onlymash.flexbooru.extension.formatDate
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class PoolMoe(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("user_id")
    val userId: Int,
    @SerialName("is_public")
    val isPublic: Boolean,
    @SerialName("post_count")
    val postCount: Int,
    @SerialName("description")
    val description: String
) {
    private fun date(): CharSequence {
        val date =  when {
            updatedAt.contains("T") -> SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.ENGLISH).parse(updatedAt)
            updatedAt.contains(" ") -> SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(updatedAt)
            else -> {
                throw IllegalStateException("Unknown date format: $updatedAt")
            }
        } ?: return ""
        return date.time.formatDate()
    }

    fun toPool(scheme: String, host: String): Pool {
        return Pool(
            booruType = BOORU_TYPE_MOE,
            scheme = scheme,
            host = host,
            id = id,
            name = name,
            count = postCount,
            date = date(),
            description = description,
            creatorId = userId
        )
    }
}