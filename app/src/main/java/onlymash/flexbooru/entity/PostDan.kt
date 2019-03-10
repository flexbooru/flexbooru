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

package onlymash.flexbooru.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "posts_danbooru", indices = [(Index(value = ["host", "keyword", "id"], unique = true))])
data class PostDan(
    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0L,
    var scheme: String = "http",
    var host: String = "",
    var keyword: String = "",
    val id: Int,
    val created_at: String,
    val uploader_id: Int,
    val score: Int,
    val source: String,
    val md5: String?,
    val last_comment_bumped_at: String?,
    val rating: String,
    val image_width: Int,
    val image_height: Int,
    val tag_string: String,
    val is_note_locked: Boolean,
    val fav_count: Int,
    val file_ext: String?,
    val last_noted_at: String?,
    val is_rating_locked: Boolean,
    val parent_id: Int?,
    val has_children: Boolean,
    val approver_id: Int?,
    val tag_count_general: Int,
    val tag_count_artist: Int,
    val tag_count_character: Int,
    val tag_count_copyright: Int,
    val file_size: Int,
    val is_status_locked: Boolean,
    val pool_string: String?,
    val up_score: Int,
    val down_score: Int,
    val is_pending: Boolean,
    val is_flagged: Boolean,
    val is_deleted: Boolean,
    val tag_count: Int,
    val updated_at: String?,
    val is_banned: Boolean,
    val pixiv_id: Int,
    val last_commented_at: String?,
    val has_active_children: Boolean,
    val bit_flags: Int,
    val tag_count_meta: Int,
    val uploader_name: String,
    val has_large: Boolean,
    val has_visible_children: Boolean,
    val children_ids: String?,
    val is_favorited: Boolean,
    val tag_string_general: String?,
    val tag_string_character: String?,
    val tag_string_copyright: String?,
    val tag_string_artist: String?,
    val tag_string_meta: String?,
    val file_url: String?,
    val large_file_url: String?,
    val preview_file_url: String?
) : BasePost() {
    // to be consistent w/ changing backend order, we need to keep a data like this
    var indexInResponse: Int = -1

    fun getUpdateDate(): String =
        updated_at ?: created_at

    override fun getPreviewUrl(): String =
            if (preview_file_url.isNullOrEmpty()) "" else checkUrl(preview_file_url)

    /**
     * return Sample url [String]
     * */
    override fun getSampleUrl(): String =
            if (large_file_url.isNullOrEmpty()) getPreviewUrl() else checkUrl(large_file_url)

    /**
     * return Larger url [String]
     * */
    override fun getLargerUrl(): String = getSampleUrl()

    /**
     * return Origin url [String]
     * */
    override fun getOriginUrl(): String = if (file_url.isNullOrBlank()) getLargerUrl() else checkUrl(file_url)

    override fun checkUrl(url: String): String =
        if (url.startsWith("http")) url else "$scheme://$host$url"
}