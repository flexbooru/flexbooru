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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.tickaroo.tikxml.annotation.Attribute
import onlymash.flexbooru.extension.formatDate

@Entity(tableName = "posts_hydrus_file", indices = [(Index(value = ["host", "keyword", "id"], unique = true))])
data class PostHydrusFileResponse(
    @Attribute(name = "id")
    @ColumnInfo(name = "id")
    val id: Int,
    @Attribute(name = "height")
    @ColumnInfo(name = "height")
    val height: Int,
    @ColumnInfo(name = "score")
    @Attribute(name = "score")
    val score: String,
    @ColumnInfo(name = "file_url")
    @Attribute(name = "file_url")
    val file_url: String,
    @ColumnInfo(name = "sample_url")
    @Attribute(name = "sample_url")
    val sample_url: String,
    @ColumnInfo(name = "sample_width")
    @Attribute(name = "sample_width")
    val sample_width: Int,
    @ColumnInfo(name = "sample_height")
    @Attribute(name = "sample_height")
    val sample_height: Int,
    @ColumnInfo(name = "preview_url")
    @Attribute(name = "preview_url")
    val preview_url: String,
    @ColumnInfo(name = "rating")
    @Attribute(name = "rating")
    val rating: String,
    @ColumnInfo(name = "tags")
    @Attribute(name = "tags")
    val tags: String,
    @ColumnInfo(name = "width")
    @Attribute(name = "width")
    val width: Int,
    @ColumnInfo(name = "change")
    @Attribute(name = "change")
    val change: Long,
    @ColumnInfo(name = "md5")
    @Attribute(name = "md5")
    val md5: String,
    @ColumnInfo(name = "creator_id")
    @Attribute(name = "creator_id")
    val creator_id: Int,
    @ColumnInfo(name = "has_children")
    @Attribute(name = "has_children")
    val has_children: Boolean,
    @ColumnInfo(name = "created_at")
    @Attribute(name = "created_at")
    val created_at:	String,
    @ColumnInfo(name = "status")
    @Attribute(name = "status")
    val status: String,
    @ColumnInfo(name = "source")
    @Attribute(name = "source")
    val source: String,
    @ColumnInfo(name = "has_notes")
    @Attribute(name = "has_notes")
    val has_notes:	Boolean,
    @ColumnInfo(name = "has_comments")
    @Attribute(name = "has_comments")
    val has_comments: Boolean,
    @ColumnInfo(name = "preview_width")
    @Attribute(name = "preview_width")
    val preview_width: Int,
    @ColumnInfo(name = "preview_height")
    @Attribute(name = "preview_height")
    val preview_height: Int
): PostBase() {
    override fun getSampleSize(): String = "$sample_width x $sample_height"

    override fun getLargerSize(): String = getSampleSize()

    override fun getOriginSize(): String = "$width x $height"

    override fun getPostId(): Int = id

    override fun getPostWidth(): Int = width

    override fun getPostHeight(): Int = height

    override fun getPostScore(): Int = if (score.isEmpty()) 0 else score.trim().toInt()

    override fun getPostRating(): String = rating

    override fun getPreviewUrl(): String = checkUrl(preview_url)

    override fun getSampleUrl(): String = checkUrl(sample_url)

    override fun getLargerUrl(): String = getSampleUrl()

    override fun getOriginUrl(): String = checkUrl(file_url)

    override fun getCreatedDate(): String =
        created_at

    override fun getUpdatedDate(): String =
        (change * 1000L).formatDate().toString()

    companion object {
        private const val PATTERN = "EEE MMM dd HH:mm:ss Z yyyy"
    }
}