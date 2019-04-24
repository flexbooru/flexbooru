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

import onlymash.flexbooru.util.formatDate
import java.text.SimpleDateFormat
import java.util.*

data class PoolDan(
    val id: Int,
    val name: String,
    val created_at: String,
    val updated_at: String,
    val creator_id: Int,
    val description: String,
    val is_active: Boolean,
    val is_deleted: Boolean,
    val category: String,
    val creator_name: String,
    val post_count: Int
) : PoolBase() {
    override fun getPoolId(): Int = id
    override fun getPoolName(): String = name
    override fun getPostCount(): Int = post_count
    override fun getPoolDate(): CharSequence {
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss", Locale.ENGLISH).parse(updated_at) ?: return ""
        return formatDate(date.time)
    }
    override fun getPoolDescription(): String = description
    override fun getCreatorId(): Int = creator_id
    override fun getCreatorName(): String? = creator_name
}