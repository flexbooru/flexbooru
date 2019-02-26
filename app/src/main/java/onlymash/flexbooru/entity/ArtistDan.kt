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

@Entity(tableName = "artists_danbooru", indices = [(Index(value = ["host", "keyword", "id"], unique = true))])
data class ArtistDan(
    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0L,
    var scheme: String = "",
    var host: String = "",
    var keyword: String? = "",
    val id: Int,
    val name: String,
    val created_at: String,
    val updated_at: String,
    val creator_id: Int,
    val is_active: Boolean,
    val group_name: String,
    val is_banned: Boolean,
    val other_names: MutableList<String>?,
    val urls: MutableList<ArtistUrlDan>?
) {
    // to be consistent w/ changing backend order, we need to keep a data like this
    var indexInResponse: Int = -1
}

data class ArtistUrlDan(
    val id: Int,
    val artist_id: Int,
    val url: String,
    val normalized_url: String,
    val created_at: String,
    val updated_at: String,
    val is_active: Boolean
)