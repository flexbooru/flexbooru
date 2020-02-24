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
import onlymash.flexbooru.extension.formatDate
import java.text.SimpleDateFormat
import java.util.*

data class PoolDan(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("is_deleted")
    val isDeleted: Boolean,
    @SerializedName("post_count")
    val postCountInt: Int,
    @SerializedName("category")
    val category: String,
    @SerializedName("updated_at")
    val updatedAt: String
) : PoolBase() {
    override fun getPoolId(): Int = id
    override fun getPoolName(): String = name
    override fun getPostCount(): Int = postCountInt
    override fun getPoolDate(): CharSequence {
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss", Locale.ENGLISH).parse(updatedAt) ?: return ""
        return date.time.formatDate()
    }
    override fun getPoolDescription(): String = description
    override fun getCreatorId(): Int = -1
    override fun getCreatorName(): String? = null
}