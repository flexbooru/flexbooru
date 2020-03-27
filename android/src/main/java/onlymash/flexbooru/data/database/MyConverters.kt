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

package onlymash.flexbooru.data.database

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import kotlinx.serialization.parseList
import kotlinx.serialization.stringify
import onlymash.flexbooru.data.model.common.TagBase
import onlymash.flexbooru.data.model.common.Uploader

/**
 * room database TypeConverter
 * */
class MyConverters {

    @TypeConverter
    fun fromStringToStringList(value: String) =
        Json.parseList<String>(value)

    @TypeConverter
    fun fromStringListToString(list: List<String>) =
        Json.stringify(list)


    @TypeConverter
    fun fromUploaderToString(uploader: Uploader) =
        Json.stringify(Uploader.serializer(), uploader)

    @TypeConverter
    fun fromStringToUploader(value: String) =
        Json.parse(Uploader.serializer(), value)

    @TypeConverter
    fun formStringToTagBase(value: String) =
        Json.parse(TagBase.serializer(), value)

    @TypeConverter
    fun formTagBaseToString(tagBase: TagBase) =
        Json.stringify(TagBase.serializer(), tagBase)

    @TypeConverter
    fun formStringToTagBaseList(value: String) =
        Json.parseList<TagBase>(value)

    @TypeConverter
    fun formTagBaseListToString(tags: List<TagBase>) =
        Json.stringify(tags)
}