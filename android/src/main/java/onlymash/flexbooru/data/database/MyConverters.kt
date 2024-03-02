/*
 * Copyright (C) 2020. by onlymash <fiepi.dev@gmail.com>, All rights reserved
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

package onlymash.flexbooru.data.database

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import onlymash.flexbooru.data.model.common.TagBase
import onlymash.flexbooru.data.model.common.User

/**
 * room database TypeConverter
 * */
class MyConverters {

    @TypeConverter
    fun fromStringToStringList(value: String): List<String> =
        Json.decodeFromString(value)

    @TypeConverter
    fun fromStringListToString(list: List<String>): String =
        Json.encodeToString(list)


    @TypeConverter
    fun fromUserToString(user: User?): String? {
        if (user == null) return null
        return Json.encodeToString(User.serializer(), user)
    }

    @TypeConverter
    fun fromStringToUser(value: String?): User? {
        if (value == null) return null
        return Json.decodeFromString(User.serializer(), value)
    }

    @TypeConverter
    fun formStringToTagBase(value: String): TagBase =
        Json.decodeFromString(TagBase.serializer(), value)

    @TypeConverter
    fun formTagBaseToString(tagBase: TagBase): String =
        Json.encodeToString(TagBase.serializer(), tagBase)

    @TypeConverter
    fun formStringToTagBaseList(value: String): List<TagBase> =
        Json.decodeFromString(value)

    @TypeConverter
    fun formTagBaseListToString(tags: List<TagBase>) =
        Json.encodeToString(tags)
}