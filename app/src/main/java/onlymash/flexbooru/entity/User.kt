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
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [(Index(value = ["booru_uid"], unique = true))],
    foreignKeys = [(ForeignKey(
            entity = Booru::class,
            parentColumns = ["uid"],
            childColumns = ["booru_uid"],
            onDelete = ForeignKey.CASCADE))])
data class User(
    @PrimaryKey(autoGenerate = true)
    var uid: Long = -1L,
    var booru_uid: Long = -1L,
    var name: String,
    var id: Int,
    var password_hash: String? = null,
    var api_key: String? = null
)