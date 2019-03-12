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

package onlymash.flexbooru.entity.post

import androidx.room.Entity
import androidx.room.Index

@Entity(tableName = "posts_moebooru", indices = [(Index(value = ["host", "keyword", "id"], unique = true))])
data class PostMoe(
    val id: Int,
    val tags: String?,
    val created_at: Int,
    val creator_id: Int,
    val author: String,
    val change: Int,
    val source: String?,
    val score: Int,
    val md5: String,
    val file_size: Int = 0,
    val file_url: String?,
    val file_ext: String?,
    val is_shown_in_index: Boolean,
    val preview_url: String,
    val preview_width: Int,
    val preview_height: Int,
    val actual_preview_width: Int,
    val actual_preview_height: Int,
    val sample_url: String?,
    val sample_width: Int,
    val sample_height: Int,
    val sample_file_size: Int = 0,
    val jpeg_url: String?,
    val jpeg_width: Int = 0,
    val jpeg_height: Int = 0,
    val jpeg_file_size: Int = 0,
    val rating: String,
    val has_children: Boolean,
    val parent_id: Int?,
    val status: String,
    val width: Int,
    val height: Int,
    val is_held: Boolean
) : BasePost() {

    override fun getPreviewUrl(): String = checkUrl(preview_url)

    /**
     * return Sample url [String]
     * */
    override fun getSampleUrl(): String =
        if (sample_url.isNullOrEmpty()) getPreviewUrl() else checkUrl(sample_url)

    /**
     * return Larger url [String]
     * */
    override fun getLargerUrl(): String =
        if (jpeg_url.isNullOrEmpty()) getSampleUrl() else checkUrl(jpeg_url)

    /**
     * return Origin url [String]
     * */
    override fun getOriginUrl(): String = if (file_url.isNullOrBlank()) getLargerUrl() else checkUrl(file_url)
}