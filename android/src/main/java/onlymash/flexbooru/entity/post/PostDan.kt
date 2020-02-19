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

package onlymash.flexbooru.entity.post

import android.text.format.Formatter
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.google.gson.annotations.SerializedName
import onlymash.flexbooru.common.App
import onlymash.flexbooru.extension.formatDate
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "posts_danbooru", indices = [(Index(value = ["host", "keyword", "id"], unique = true))])
data class PostDan(
    @ColumnInfo(name = "id")
    @SerializedName("id")
    val id: Int,
    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    val created_at: String,
    @ColumnInfo(name = "uploader_id")
    @SerializedName("uploader_id")
    val uploader_id: Int,
    @ColumnInfo(name = "score")
    @SerializedName("score")
    val score: Int,
    @ColumnInfo(name = "source")
    @SerializedName("source")
    val source: String,
    @ColumnInfo(name = "md5")
    @SerializedName("md5")
    val md5: String?,
    @ColumnInfo(name = "last_comment_bumped_at")
    @SerializedName("last_comment_bumped_at")
    val last_comment_bumped_at: String?,
    @ColumnInfo(name = "rating")
    @SerializedName("rating")
    val rating: String,
    @ColumnInfo(name = "image_width")
    @SerializedName("image_width")
    val image_width: Int,
    @ColumnInfo(name = "image_height")
    @SerializedName("image_height")
    val image_height: Int,
    @ColumnInfo(name = "tag_string")
    @SerializedName("tag_string")
    val tag_string: String,
    @ColumnInfo(name = "is_note_locked")
    @SerializedName("is_note_locked")
    val is_note_locked: Boolean,
    @ColumnInfo(name = "fav_count")
    @SerializedName("fav_count")
    val fav_count: Int,
    @ColumnInfo(name = "file_ext")
    @SerializedName("file_ext")
    val file_ext: String?,
    @ColumnInfo(name = "last_noted_at")
    @SerializedName("last_noted_at")
    val last_noted_at: String?,
    @ColumnInfo(name = "is_rating_locked")
    @SerializedName("is_rating_locked")
    val is_rating_locked: Boolean,
    @ColumnInfo(name = "parent_id")
    @SerializedName("parent_id")
    val parent_id: Int?,
    @ColumnInfo(name = "has_children")
    @SerializedName("has_children")
    val has_children: Boolean,
    @ColumnInfo(name = "approver_id")
    @SerializedName("approver_id")
    val approver_id: Int?,
    @ColumnInfo(name = "tag_count_general")
    @SerializedName("tag_count_general")
    val tag_count_general: Int,
    @ColumnInfo(name = "tag_count_artist")
    @SerializedName("tag_count_artist")
    val tag_count_artist: Int,
    @ColumnInfo(name = "tag_count_character")
    @SerializedName("tag_count_character")
    val tag_count_character: Int,
    @ColumnInfo(name = "tag_count_copyright")
    @SerializedName("tag_count_copyright")
    val tag_count_copyright: Int,
    @ColumnInfo(name = "file_size")
    @SerializedName("file_size")
    val file_size: Int,
    @ColumnInfo(name = "is_status_locked")
    @SerializedName("is_status_locked")
    val is_status_locked: Boolean,
    @ColumnInfo(name = "pool_string")
    @SerializedName("pool_string")
    val pool_string: String?,
    @ColumnInfo(name = "up_score")
    @SerializedName("up_score")
    val up_score: Int,
    @ColumnInfo(name = "down_score")
    @SerializedName("down_score")
    val down_score: Int,
    @ColumnInfo(name = "is_pending")
    @SerializedName("is_pending")
    val is_pending: Boolean,
    @ColumnInfo(name = "is_flagged")
    @SerializedName("is_flagged")
    val is_flagged: Boolean,
    @ColumnInfo(name = "is_deleted")
    @SerializedName("is_deleted")
    val is_deleted: Boolean,
    @ColumnInfo(name = "tag_count")
    @SerializedName("tag_count")
    val tag_count: Int,
    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at")
    val updated_at: String?,
    @ColumnInfo(name = "is_banned")
    @SerializedName("is_banned")
    val is_banned: Boolean,
    @ColumnInfo(name = "pixiv_id")
    @SerializedName("pixiv_id")
    val pixiv_id: Int,
    @ColumnInfo(name = "last_commented_at")
    @SerializedName("last_commented_at")
    val last_commented_at: String?,
    @ColumnInfo(name = "has_active_children")
    @SerializedName("has_active_children")
    val has_active_children: Boolean,
    @ColumnInfo(name = "bit_flags")
    @SerializedName("bit_flags")
    val bit_flags: Int,
    @ColumnInfo(name = "tag_count_meta")
    @SerializedName("tag_count_meta")
    val tag_count_meta: Int,
    @ColumnInfo(name = "uploader_name")
    @SerializedName("uploader_name")
    val uploader_name: String?,
    @ColumnInfo(name = "has_large")
    @SerializedName("has_large")
    val has_large: Boolean,
    @ColumnInfo(name = "has_visible_children")
    @SerializedName("has_visible_children")
    val has_visible_children: Boolean,
    @ColumnInfo(name = "children_ids")
    @SerializedName("children_ids")
    val children_ids: String?,
    @ColumnInfo(name = "is_favorited")
    @SerializedName("is_favorited")
    val is_favorited: Boolean,
    @ColumnInfo(name = "tag_string_general")
    @SerializedName("tag_string_general")
    val tag_string_general: String?,
    @ColumnInfo(name = "tag_string_character")
    @SerializedName("tag_string_character")
    val tag_string_character: String?,
    @ColumnInfo(name = "tag_string_copyright")
    @SerializedName("tag_string_copyright")
    val tag_string_copyright: String?,
    @ColumnInfo(name = "tag_string_artist")
    @SerializedName("tag_string_artist")
    val tag_string_artist: String?,
    @ColumnInfo(name = "tag_string_meta")
    @SerializedName("tag_string_meta")
    val tag_string_meta: String?,
    @ColumnInfo(name = "file_url")
    @SerializedName("file_url")
    val file_url: String?,
    @ColumnInfo(name = "large_file_url")
    @SerializedName("large_file_url")
    val large_file_url: String?,
    @ColumnInfo(name = "preview_file_url")
    @SerializedName("preview_file_url")
    val preview_file_url: String?
) : PostBase() {
    override fun getSampleSize(): String = "not data"

    override fun getLargerSize(): String = "not data"

    override fun getOriginSize(): String = "$image_width x $image_height ${Formatter.formatFileSize(App.app, file_size.toLong())}"

    override fun getPostId(): Int = id

    override fun getPostWidth(): Int = image_width

    override fun getPostHeight(): Int = image_height

    override fun getPostScore(): Int = score

    override fun getPostRating(): String = rating

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

    override fun getCreatedDate(): String =
        SimpleDateFormat(PATTERN, Locale.ENGLISH)
            .parse(created_at)?.time?.formatDate().toString()

    override fun getUpdatedDate(): String =
        SimpleDateFormat(PATTERN, Locale.ENGLISH)
            .parse(updated_at ?: created_at)?.time?.formatDate().toString()

    companion object {
        private const val PATTERN = "yyyy-MM-dd'T'HH:mm:ss.sss"
    }
}