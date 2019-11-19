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

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class PostHydrusFileId(
    @SerializedName("file_ids")
    val file_ids: List<Int>
): PostBase() {
    override fun getSampleSize(): String = "500 x 500"

    override fun getLargerSize(): String = getSampleSize()

    override fun getOriginSize(): String = "1000 x 1000"

    override fun getPostId(): Int = 123

    override fun getPostWidth(): Int = 800

    override fun getPostHeight(): Int = 800

    override fun getPostScore(): Int = 0

    override fun getPostRating(): String = "explicit"

    override fun getPreviewUrl(): String = checkUrl("http://192.168.100.122:45869/get_files/file?Hydrus-Client-API-Access-Key=067fde0b3e566f5dd4a3874621669e33b710286b0b5d7cd201b414b6541347a4&file_id="+file_ids[0])

    override fun getSampleUrl(): String = checkUrl("\"http://192.168.100.122:45869/get_files/file?Hydrus-Client-API-Access-Key=067fde0b3e566f5dd4a3874621669e33b710286b0b5d7cd201b414b6541347a4&file_id=\"+file_ids[0]")

    override fun getLargerUrl(): String = getSampleUrl()

    override fun getOriginUrl(): String = checkUrl("\"http://192.168.100.122:45869/get_files/file?Hydrus-Client-API-Access-Key=067fde0b3e566f5dd4a3874621669e33b710286b0b5d7cd201b414b6541347a4&file_id=\"+file_ids[0]")

    override fun getCreatedDate(): String = ""

    override fun getUpdatedDate(): String = ""

    companion object {
        private const val PATTERN = "EEE MMM dd HH:mm:ss Z yyyy"
    }
}


class GithubTypeConverters {

    internal var gson: Gson = Gson()

    @TypeConverter
    fun stringToSomeObjectList(data: String?): List<Int> {
        if (data == null) {
            return emptyList()
        }

        val listType = object : com.google.gson.reflect.TypeToken<List<Int>>() {

        }.type

        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun someObjectListToString(someObjects: List<Int>): String {
        return gson.toJson(someObjects)
    }
}