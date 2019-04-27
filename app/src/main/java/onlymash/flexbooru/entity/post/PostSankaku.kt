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
import androidx.room.Entity
import androidx.room.Index
import onlymash.flexbooru.App
import onlymash.flexbooru.entity.DanOneDate
import onlymash.flexbooru.entity.SankakuAuthor
import onlymash.flexbooru.entity.SankakuTag
import onlymash.flexbooru.util.formatDate

@Entity(tableName = "posts_sankaku", indices = [(Index(value = ["host", "keyword", "id"], unique = true))])
data class PostSankaku(
    val author: SankakuAuthor,
    val change: Int,
    val created_at: DanOneDate,
    val fav_count: Int,
    val file_size: Int,
    val file_type: String?,
    val file_url: String,
    val has_children: Boolean,
    val has_comments: Boolean,
    val has_notes: Boolean,
    val height: Int,
    val id: Int,
    val in_visible_pool: Boolean,
    val is_favorited: Boolean,
    val is_premium: Boolean,
    val md5: String,
    val parent_id: Int?,
    val preview_height: Int,
    val preview_url: String,
    val preview_width: Int,
    val rating: String?,
    val recommended_posts: Int,
    val recommended_score: Int,
    val sample_height: Int,
    val sample_url: String,
    val sample_width: Int,
    val source: String?,
    val status: String,
    val tags: List<SankakuTag>,
    val total_score: Int,
    val vote_count: Int,
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

    override fun getCreatedDate(): String = formatDate(created_at.s * 1000L).toString()

    override fun getUpdatedDate(): String = formatDate(change * 1000L).toString()

}