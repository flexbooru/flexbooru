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

@Entity(tableName = "posts_danbooru_one", indices = [(Index(value = ["host", "keyword", "id"], unique = true))])
data class PostDanOne(
    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0L,
    var host: String = "",
    var keyword: String = "",
    val id: Int,
    val status: String,
    val creator_id: Int,
    val preview_width: Int,
    val source: String?,
    val author: String,
    val width: Int,
    val score: Int,
    val preview_height: Int,
    val has_comments: Boolean,
    val sample_width: Int,
    val has_children: Boolean,
    val sample_url: String?,
    val file_url: String?,
    val parent_id: Int?,
    val sample_height: Int,
    val md5: String,
    val tags: String,
    val change: Long,
    val has_notes: Boolean,
    val rating: String,
    val height: Int,
    val preview_url: String,
    val file_size: Int,
    val created_at: DanOneDate
) {
    // to be consistent w/ changing backend order, we need to keep a data like this
    var indexInResponse: Int = -1

    /**
     * return Sample url [String]
     * */
    fun getSampleUrl(): String = sample_url ?: preview_url ?: ""

    /**
     * return Larger url [String]
     * */
    fun getLargerUrl(): String = getSampleUrl()

    /**
     * return Origin url [String]
     * */
    fun getOriginUrl(): String = if (file_url.isNullOrBlank()) getLargerUrl() else file_url
}