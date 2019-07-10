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

import com.google.gson.annotations.SerializedName
import onlymash.flexbooru.extension.formatDate
import java.text.SimpleDateFormat
import java.util.*

/**
 * Danbooru response data class
 * */
data class CommentDan(
    @SerializedName("id")
    val id: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("post_id")
    val post_id: Int,
    @SerializedName("creator_id")
    val creator_id: Int,
    @SerializedName("body")
    val body: String,
    @SerializedName("score")
    val score: Int,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("updater_id")
    val updaterId: Int,
    @SerializedName("do_not_bump_post")
    val doNotBumpPost: Boolean,
    @SerializedName("is_deleted")
    val isDeleted: Boolean,
    @SerializedName("is_sticky")
    val isSticky: Boolean,
    @SerializedName("creator_name")
    val creator_name: String,
    @SerializedName("updater_name")
    val updaterName: String
) : CommentBase() {
    override fun getPostId(): Int = post_id
    override fun getCommentId(): Int = id
    override fun getCommentBody(): String = body
    override fun getCommentDate(): CharSequence {
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss", Locale.ENGLISH).parse(updatedAt) ?: return ""
        return date.time.formatDate()
    }
    override fun getCreatorId(): Int = creator_id
    override fun getCreatorName(): String = creator_name
}