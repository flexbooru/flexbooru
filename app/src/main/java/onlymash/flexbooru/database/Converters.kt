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

package onlymash.flexbooru.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import onlymash.flexbooru.entity.ArtistUrlDan

class Converters {

    @TypeConverter
    fun fromStringToStringList(value: String): MutableList<String>? {
        val listType = object : TypeToken<MutableList<String>>(){}.type
        return Gson().fromJson<MutableList<String>>(value, listType)
    }

    @TypeConverter
    fun fromStringListToString(list: MutableList<String>): String =
        Gson().toJson(list)

    @TypeConverter
    fun fromStringToUrlDanList(value: String): MutableList<ArtistUrlDan>? {
        val listType = object : TypeToken<MutableList<ArtistUrlDan>>(){}.type
        return Gson().fromJson<MutableList<ArtistUrlDan>>(value, listType)
    }

    @TypeConverter
    fun fromUrlDanListToString(list: MutableList<ArtistUrlDan>): String =
            Gson().toJson(list)
}