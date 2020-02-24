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
    @SerializedName("body")
    val body: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("creator")
    val creator: Creator,
    @SerializedName("id")
    val id: Int,
    @SerializedName("post_id")
    val postIdInt: Int
) : CommentBase() {
    override fun getPostId(): Int = postIdInt
    override fun getCommentId(): Int = id
    override fun getCommentBody(): String = body
    override fun getCommentDate(): CharSequence {
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss", Locale.ENGLISH).parse(createdAt) ?: return ""
        return date.time.formatDate()
    }
    override fun getCreatorId(): Int = creator.id
    override fun getCreatorName(): String = creator.name
}

data class Creator(
    @SerializedName("can_approve_posts")
    val canApprovePosts: Boolean,
    @SerializedName("can_upload_free")
    val canUploadFree: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("is_banned")
    val isBanned: Boolean,
    @SerializedName("is_super_voter")
    val isSuperVoter: Boolean,
    @SerializedName("level")
    val level: Int,
    @SerializedName("level_string")
    val levelString: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("note_update_count")
    val noteUpdateCount: Int,
    @SerializedName("post_update_count")
    val postUpdateCount: Int,
    @SerializedName("post_upload_count")
    val postUploadCount: Int
)