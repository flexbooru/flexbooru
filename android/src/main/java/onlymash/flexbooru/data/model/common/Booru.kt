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

package onlymash.flexbooru.data.model.common

import android.net.Uri
import android.util.Base64
import androidx.core.net.toUri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import okhttp3.HttpUrl
import java.util.*

@Serializable
@Entity(tableName = "boorus", indices = [(Index(value = ["scheme", "host"], unique = true))])
data class Booru(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "uid")
    @Transient
    var uid: Long = 0L,
    @ColumnInfo(name = "name")
    @SerialName("name")
    var name: String,
    @ColumnInfo(name = "scheme")
    @SerialName("scheme")
    var scheme: String = "https",
    @ColumnInfo(name = "host")
    @SerialName("host")
    var host: String,
    @ColumnInfo(name = "hash_salt")
    @SerialName("hash_salt")
    var hashSalt: String = "",
    // 0: danbooru 1: moebooru
    @ColumnInfo(name = "type")
    @SerialName("type")
    var type: Int,
    @ColumnInfo(name = "blacklists")
    @SerialName("blacklists")
    var blacklists: List<String> = listOf(),
    @ColumnInfo(name = "user")
    @SerialName("user")
    var user: User? = null,
    @ColumnInfo(name = "path")
    @SerialName("path")
    var path: String? = null,
    @ColumnInfo(name = "auth")
    @SerialName("auth")
    var auth: String? = null
) {
    fun getBlacklistsString(): String {
        var tagsString = ""
        blacklists.forEach {
            tagsString = "$tagsString -$it"
        }
        return tagsString.trim()
    }

    fun getDan1UserUrl(username: String): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("user")
            .addPathSegment("index.json")
            .addQueryParameter("name", username)
            .build()
    }
    
    fun getDan1UserUrlById(id: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("user")
            .addPathSegment("index.json")
            .addQueryParameter("id", id.toString())
            .build()
    }

    fun getDanUserUrl(username: String): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("users.json")
            .addQueryParameter("search[name]", username)
            .build()
    }

    fun getMoeUserUrl(username: String): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("user.json")
            .addQueryParameter("name", username)
            .build()
    }

    fun getMoeUserUrlById(id: Int): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("user.json")
            .addQueryParameter("id", id.toString())
            .build()
    }

    fun getMoeCheckUserUrl(): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegments("user/check.json")
            .build()
    }

    fun getSankakuUserUrl(username: String): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("users")
            .addQueryParameter("name", username)
            .build()
    }

    fun getSankakuTokenUrl(): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment("auth")
            .addPathSegment("token")
            .build()
    }
    
    override fun toString(): String = toUri().toString()

    private fun toUri(): Uri {
        return Uri.Builder()
            .scheme("booru")
            .encodedAuthority(String.format(Locale.ENGLISH, "%s",
                Base64.encodeToString(getString().toByteArray(), Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE)))
            .build()
    }

    private fun getString(): String {
        return if (path.isNullOrBlank()) {
            "$name@@@$scheme@@@$host@@@$type@@@$hashSalt"
        } else {
            "$name@@@$scheme@@@$host@@@$type@@@$hashSalt@@@$path"
        }
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
                when (dataList.size) {
                    5 -> {
                        Booru(
                            name = dataList[0],
                            scheme = dataList[1],
                            host = dataList[2],
                            type = dataList[3].toInt(),
                            hashSalt = dataList[4]
                        )
                    }
                    6 -> {
                        Booru(
                            name = dataList[0],
                            scheme = dataList[1],
                            host = dataList[2],
                            type = dataList[3].toInt(),
                            hashSalt = dataList[4],
                            path = dataList[5]
                        )
                    }
                    else -> {
                        null
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                null
            }
        }
    }
}