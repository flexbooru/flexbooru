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

package onlymash.flexbooru.entity.pool

import com.google.gson.annotations.SerializedName
import onlymash.flexbooru.entity.common.DanOneDate
import onlymash.flexbooru.extension.formatDate

data class PoolDanOne(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("is_public")
    val isPublic: Boolean,
    @SerializedName("post_count")
    val post_count: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("updated_at")
    val updatedAt: DanOneDate,
    @SerializedName("id")
    val id: Int,
    @SerializedName("created_at")
    val createdAt: DanOneDate
) : PoolBase() {
    override fun getPoolId(): Int = id
    override fun getPoolName(): String = name
    override fun getPostCount(): Int = post_count
    override fun getPoolDate(): CharSequence = (updatedAt.s * 1000L).formatDate()
    override fun getPoolDescription(): String = ""
    override fun getCreatorId(): Int = userId
    override fun getCreatorName(): String? = null
}