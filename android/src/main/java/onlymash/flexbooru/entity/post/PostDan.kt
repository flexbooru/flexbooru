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
    @ColumnInfo(name = "pixiv_id")
    @SerializedName("pixiv_id")
    val pixivId: Int?,
    @ColumnInfo(name = "parent_id")
    @SerializedName("parent_id")
    val parentId: Int?,
    @ColumnInfo(name = "rating")
    @SerializedName("rating")
    val rating: String,
    @ColumnInfo(name = "score")
    @SerializedName("score")
    val score: Int,
    @ColumnInfo(name = "source")
    @SerializedName("source")
    val source: String,
    @ColumnInfo(name = "fav_count")
    @SerializedName("fav_count")
    val favCount: Int,
    @ColumnInfo(name = "image_height")
    @SerializedName("image_height")
    val imageHeight: Int,
    @ColumnInfo(name = "image_width")
    @SerializedName("image_width")
    val imageWidth: Int,
    @ColumnInfo(name = "file_ext")
    @SerializedName("file_ext")
    val fileExt: String,
    @ColumnInfo(name = "file_size")
    @SerializedName("file_size")
    val fileSize: Int,
    @ColumnInfo(name = "preview_file_url")
    @SerializedName("preview_file_url")
    val previewFileUrl: String?,
    @ColumnInfo(name = "large_file_url")
    @SerializedName("large_file_url")
    val largeFileUrl: String?,
    @ColumnInfo(name = "file_url")
    @SerializedName("file_url")
    val fileUrl: String?,
    @ColumnInfo(name = "tag_string")
    @SerializedName("tag_string")
    val tagString: String,
    @ColumnInfo(name = "tag_string_artist")
    @SerializedName("tag_string_artist")
    val tagStringArtist: String,
    @ColumnInfo(name = "tag_string_character")
    @SerializedName("tag_string_character")
    val tagStringCharacter: String,
    @ColumnInfo(name = "tag_string_copyright")
    @SerializedName("tag_string_copyright")
    val tagStringCopyright: String,
    @ColumnInfo(name = "tag_string_general")
    @SerializedName("tag_string_general")
    val tagStringGeneral: String,
    @ColumnInfo(name = "tag_string_meta")
    @SerializedName("tag_string_meta")
    val tagStringMeta: String,
    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    val createdAt: String,
    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at")
    val updatedAt: String?,
    @ColumnInfo(name = "is_favorited")
    @SerializedName("is_favorited")
    val isFavorited: Boolean = false,
    @ColumnInfo(name = "uploader")
    @SerializedName("uploader")
    val uploader: Uploader
) : PostBase() {
    override fun getSampleSize(): String = "not data"

    override fun getLargerSize(): String = "not data"

    override fun getOriginSize(): String = "$imageWidth x $imageHeight ${Formatter.formatFileSize(App.app, fileSize.toLong())}"

    override fun getPostId(): Int = id

    override fun getPostWidth(): Int = imageWidth

    override fun getPostHeight(): Int = imageHeight

    override fun getPostScore(): Int = score

    override fun getPostRating(): String = rating

    override fun getPreviewUrl(): String =
            if (previewFileUrl.isNullOrEmpty()) "" else checkUrl(previewFileUrl)

    /**
     * return Sample url [String]
     * */
    override fun getSampleUrl(): String =
            if (largeFileUrl.isNullOrEmpty()) getPreviewUrl() else checkUrl(largeFileUrl)

    /**
     * return Larger url [String]
     * */
    override fun getLargerUrl(): String = getSampleUrl()

    /**
     * return Origin url [String]
     * */
    override fun getOriginUrl(): String = if (fileUrl.isNullOrBlank()) getLargerUrl() else checkUrl(fileUrl)

    override fun getCreatedDate(): String =
        SimpleDateFormat(PATTERN, Locale.ENGLISH)
            .parse(createdAt)?.time?.formatDate().toString()

    override fun getUpdatedDate(): String =
        SimpleDateFormat(PATTERN, Locale.ENGLISH)
            .parse(updatedAt ?: createdAt)?.time?.formatDate().toString()

    companion object {
        private const val PATTERN = "yyyy-MM-dd'T'HH:mm:ss.sss"
    }
}

data class Uploader(
    @SerializedName("can_approve_posts")
    val canApprovePosts: Boolean,
    @SerializedName("can_upload_free")
    val canUploadFree: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("inviter_id")
    val inviterId: Int?,
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