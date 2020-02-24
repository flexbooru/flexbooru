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

package onlymash.flexbooru.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import onlymash.flexbooru.entity.artist.ArtistUrlDan
import onlymash.flexbooru.entity.common.DanOneDate
import onlymash.flexbooru.entity.common.SankakuAuthor
import onlymash.flexbooru.entity.common.SankakuTag
import onlymash.flexbooru.entity.post.Uploader

/**
 * room database TypeConverter
 * */
class Converters {

    /**
     * string to list
     * */
    @TypeConverter
    fun fromStringToStringList(value: String): MutableList<String>? {
        val listType = object : TypeToken<MutableList<String>>(){}.type
        return Gson().fromJson<MutableList<String>>(value, listType)
    }

    /**
     * list to json string
     * */
    @TypeConverter
    fun fromStringListToString(list: MutableList<String>): String =
        Gson().toJson(list)

    /**
     * string to danbooru artist url list
     * */
    @TypeConverter
    fun fromStringToUrlDanList(value: String): MutableList<ArtistUrlDan>? {
        val listType = object : TypeToken<MutableList<ArtistUrlDan>>(){}.type
        return Gson().fromJson<MutableList<ArtistUrlDan>>(value, listType)
    }

    /**
     * danbooru artist url list to string
     * */
    @TypeConverter
    fun fromUrlDanListToString(list: MutableList<ArtistUrlDan>): String =
        Gson().toJson(list)

    @TypeConverter
    fun fromDanOneCreatedAtToString(createdAt: DanOneDate): String =
        Gson().toJson(createdAt)

    @TypeConverter
    fun fromStringToDanOneCreatedAt(value: String): DanOneDate =
        Gson().fromJson<DanOneDate>(value, object : TypeToken<DanOneDate>(){}.type)

    @TypeConverter
    fun fromSankakuAuthorToString(author: SankakuAuthor): String =
        Gson().toJson(author)

    @TypeConverter
    fun fromStringToSankakuAuthor(value: String): SankakuAuthor =
        Gson().fromJson<SankakuAuthor>(value, object : TypeToken<SankakuAuthor>(){}.type)

    @TypeConverter
    fun fromSankakuTagToString(tag: SankakuTag): String =
        Gson().toJson(tag)

    @TypeConverter
    fun fromStringToSankakuTag(value: String): SankakuTag =
        Gson().fromJson<SankakuTag>(value, object : TypeToken<SankakuTag>(){}.type)

    @TypeConverter
    fun fromSankakuTagListToString(tags: MutableList<SankakuTag>): String =
        Gson().toJson(tags)

    @TypeConverter
    fun fromStringToSankakuTagList(value: String): MutableList<SankakuTag> {
        val listType = object : TypeToken<MutableList<SankakuTag>>(){}.type
        return Gson().fromJson<MutableList<SankakuTag>>(value, listType)
    }

    @TypeConverter
    fun fromUploaderToString(uploader: Uploader): String =
        Gson().toJson(uploader)

    @TypeConverter
    fun fromStringToUploader(value: String): Uploader =
        Gson().fromJson<Uploader>(value, object : TypeToken<Uploader>(){}.type)
}