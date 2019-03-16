/*
 * Copyright (C) 2019 by onlymash <im@fiepi.me>, All rights reserved
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

import com.crashlytics.android.Crashlytics
import onlymash.flexbooru.util.formatDate
import java.text.SimpleDateFormat
import java.util.*

data class PoolMoe(
    val id: Int,
    val name: String,
    val created_at: String,
    val updated_at: String,
    val user_id: Int,
    val is_public: Boolean,
    val post_count: Int,
    val description: String
) : BasePool() {
    override fun getPoolId(): Int = id
    override fun getPoolName(): String = name
    override fun getPostCount(): Int = post_count
    override fun getPoolDate(): CharSequence {
        val date =  when {
            updated_at.contains("T") -> SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.ENGLISH).parse(updated_at)
            updated_at.contains(" ") -> SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(updated_at)
            else -> {
                Crashlytics.log("Unknown date format: $updated_at. Host: $host")
                throw IllegalStateException("Unknown date format: $updated_at")
            }
        } ?: return ""
        return formatDate(date.time)
    }
    override fun getPoolDescription(): String = description
    override fun getCreatorId(): Int = user_id
    override fun getCreatorName(): String? = null
}