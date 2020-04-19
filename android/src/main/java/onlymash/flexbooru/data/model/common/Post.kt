/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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

package onlymash.flexbooru.data.model.common

import androidx.room.*


@Entity(tableName = "posts", indices = [(Index(value = ["booru_uid", "query", "id"], unique = true))],
    foreignKeys = [(ForeignKey(
        entity = Booru::class,
        parentColumns = ["uid"],
        childColumns = ["booru_uid"],
        onDelete = ForeignKey.CASCADE))])
data class Post(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "uid")
    var uid: Long = 0L,
    @ColumnInfo(name = "booru_uid")
    var booruUid: Long = -1L,
    @ColumnInfo(name = "index")
    var index: Int = -1,
    @ColumnInfo(name = "query")
    var query: String = "",

    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "width")
    val width: Int,
    @ColumnInfo(name = "height")
    val height: Int,
    @ColumnInfo(name = "size")
    val size: Int,
    @ColumnInfo(name = "score")
    val score: Int,
    @ColumnInfo(name = "rating")
    val rating: String,
    @ColumnInfo(name = "is_favored")
    var isFavored: Boolean = false,
    @ColumnInfo(name = "time")
    val time: Long?,
    @ColumnInfo(name = "tags")
    val tags: List<TagBase>,
    @ColumnInfo(name = "preview")
    val preview: String,
    @ColumnInfo(name = "sample")
    val sample: String,
    @ColumnInfo(name = "medium")
    val medium: String,
    @ColumnInfo(name = "origin")
    val origin: String,
    @ColumnInfo(name = "pixiv_id")
    val pixivId: Int? = null,
    @ColumnInfo(name = "source")
    val source: String?,
    @ColumnInfo(name = "uploader")
    val uploader: User
)