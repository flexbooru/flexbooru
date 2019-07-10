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
import onlymash.flexbooru.entity.common.DanOneDate
import onlymash.flexbooru.entity.common.SankakuAuthor
import onlymash.flexbooru.entity.common.SankakuTag
import onlymash.flexbooru.extension.formatDate

@Entity(tableName = "posts_sankaku", indices = [(Index(value = ["host", "keyword", "id"], unique = true))])
data class PostSankaku(
    @ColumnInfo(name = "author")
    @SerializedName("author")
    val author: SankakuAuthor,
    @ColumnInfo(name = "change")
    @SerializedName("change")
    val change: Int,
    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    val created_at: DanOneDate,
    @ColumnInfo(name = "fav_count")
    @SerializedName("fav_count")
    val fav_count: Int,
    @ColumnInfo(name = "file_size")
    @SerializedName("file_size")
    val file_size: Int,
    @ColumnInfo(name = "file_type")
    @SerializedName("file_type")
    val file_type: String?,
    @ColumnInfo(name = "file_url")
    @SerializedName("file_url")
    val file_url: String,
    @ColumnInfo(name = "has_children")
    @SerializedName("has_children")
    val has_children: Boolean,
    @ColumnInfo(name = "has_comments")
    @SerializedName("has_comments")
    val has_comments: Boolean,
    @ColumnInfo(name = "has_notes")
    @SerializedName("has_notes")
    val has_notes: Boolean,
    @ColumnInfo(name = "height")
    @SerializedName("height")
    val height: Int,
    @ColumnInfo(name = "id")
    @SerializedName("id")
    val id: Int,
    @ColumnInfo(name = "in_visible_pool")
    @SerializedName("in_visible_pool")
    val in_visible_pool: Boolean,
    @ColumnInfo(name = "is_favorited")
    @SerializedName("is_favorited")
    val is_favorited: Boolean,
    @ColumnInfo(name = "is_premium")
    @SerializedName("is_premium")
    val is_premium: Boolean,
    @ColumnInfo(name = "md5")
    @SerializedName("md5")
    val md5: String,
    @ColumnInfo(name = "parent_id")
    @SerializedName("parent_id")
    val parent_id: Int?,
    @ColumnInfo(name = "preview_height")
    @SerializedName("preview_height")
    val preview_height: Int,
    @ColumnInfo(name = "preview_url")
    @SerializedName("preview_url")
    val preview_url: String,
    @ColumnInfo(name = "preview_width")
    @SerializedName("preview_width")
    val preview_width: Int,
    @ColumnInfo(name = "rating")
    @SerializedName("rating")
    val rating: String?,
    @ColumnInfo(name = "recommended_posts")
    @SerializedName("recommended_posts")
    val recommended_posts: Int,
    @ColumnInfo(name = "recommended_score")
    @SerializedName("recommended_score")
    val recommended_score: Int,
    @ColumnInfo(name = "sample_height")
    @SerializedName("sample_height")
    val sample_height: Int,
    @ColumnInfo(name = "sample_url")
    @SerializedName("sample_url")
    val sample_url: String,
    @ColumnInfo(name = "sample_width")
    @SerializedName("sample_width")
    val sample_width: Int,
    @ColumnInfo(name = "source")
    @SerializedName("source")
    val source: String?,
    @ColumnInfo(name = "status")
    @SerializedName("status")
    val status: String,
    @ColumnInfo(name = "tags")
    @SerializedName("tags")
    val tags: List<SankakuTag>,
    @ColumnInfo(name = "total_score")
    @SerializedName("total_score")
    val total_score: Int,
    @ColumnInfo(name = "vote_count")
    @SerializedName("vote_count")
    val vote_count: Int,
    @ColumnInfo(name = "width")
    @SerializedName("width")
    val width: Int
) : PostBase() {
    override fun getSampleSize(): String =
        "$sample_width x $sample_height"

    override fun getLargerSize(): String = getSampleSize()

    override fun getOriginSize(): String =
        "$width x $height ${Formatter.formatFileSize(App.app, file_size.toLong())}"

    override fun getPostId(): Int = id

    override fun getPostWidth(): Int = width

    override fun getPostHeight(): Int = height

    override fun getPostScore(): Int = total_score

    override fun getPostRating(): String = rating ?: "e"

    override fun getPreviewUrl(): String = checkUrl(preview_url)

    override fun getSampleUrl(): String = checkUrl(sample_url)

    override fun getLargerUrl(): String = getSampleUrl()

    override fun getOriginUrl(): String = checkUrl(file_url)

    override fun getCreatedDate(): String = (created_at.s * 1000L).formatDate().toString()

    override fun getUpdatedDate(): String = (change * 1000L).formatDate().toString()

}