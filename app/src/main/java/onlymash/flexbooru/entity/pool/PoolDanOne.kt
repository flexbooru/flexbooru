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

import onlymash.flexbooru.entity.DanOneDate
import onlymash.flexbooru.util.formatDate

data class PoolDanOne(
    val user_id: Int,
    val is_public: Boolean,
    val post_count: Int,
    val name: String,
    val updated_at: DanOneDate,
    val id: Int,
    val created_at: DanOneDate
) : PoolBase() {
    override fun getPoolId(): Int = id
    override fun getPoolName(): String = name
    override fun getPostCount(): Int = post_count
    override fun getPoolDate(): CharSequence = formatDate(updated_at.s * 1000L)
    override fun getPoolDescription(): String = ""
    override fun getCreatorId(): Int = user_id
    override fun getCreatorName(): String? = null
}