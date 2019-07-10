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

package onlymash.flexbooru.entity.common

import android.net.Uri
import android.util.Base64
import androidx.core.net.toUri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(tableName = "boorus", indices = [(Index(value = ["scheme", "host"], unique = true))])
data class Booru(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "uid")
    @SerializedName("uid")
    var uid: Long = 0L,
    @ColumnInfo(name = "name")
    @SerializedName("name")
    var name: String,
    @ColumnInfo(name = "scheme")
    @SerializedName("scheme")
    var scheme: String,
    @ColumnInfo(name = "host")
    @SerializedName("host")
    var host: String,
    @ColumnInfo(name = "hash_salt")
    @SerializedName("hash_salt")
    var hashSalt: String = "",
    // 0: danbooru 1: moebooru
    @ColumnInfo(name = "type")
    @SerializedName("type")
    var type: Int
) {
    override fun toString(): String = toUri().toString()

    private fun toUri(): Uri {
        return Uri.Builder()
            .scheme("booru")
            .encodedAuthority(String.format(Locale.ENGLISH, "%s",
                Base64.encodeToString("$name@@@$scheme@@@$host@@@$type@@@$hashSalt".toByteArray(),
                    Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE)))
            .build()
    }

    companion object {
        /**
         * String to Booru
         * */
        fun url2Booru(str: String): Booru? {
            return try {
                val dataByte = Base64.decode(str.toUri().authority,
                    Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE)
                val dataList = String(dataByte).split("@@@")
                if (dataList.size == 5) {
                    Booru(
                        uid = -1, name = dataList[0], scheme = dataList[1],
                        host = dataList[2], type = dataList[3].toInt(), hashSalt = dataList[4]
                    )
                } else {
                    null
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                null
            }
        }
    }
}