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

package onlymash.flexbooru.entity.comment

import com.crashlytics.android.Crashlytics
import onlymash.flexbooru.extension.formatDate
import java.text.SimpleDateFormat
import java.util.*

/**
 * Moebooru response data class
 * */
data class CommentMoe(
    var scheme: String = "http",
    var host: String = "",
    val id: Int,
    val created_at: String,
    val post_id: Int,
    val creator: String,
    val creator_id: Int,
    val body: String
) : CommentBase() {
    override fun getPostId(): Int = post_id
    override fun getCommentId(): Int = id
    override fun getCommentBody(): String = body
    override fun getCommentDate(): CharSequence {
        val date =  when {
            created_at.contains("T") -> SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.ENGLISH).parse(created_at)
            created_at.contains(" ") -> SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(created_at)
            else -> {
                Crashlytics.log("Unknown date format: $created_at. Host: $host")
                throw IllegalStateException("Unknown date format: $created_at")
            }
        } ?: return ""
        return date.time.formatDate()
    }
    override fun getCreatorId(): Int = creator_id
    override fun getCreatorName(): String = creator
}