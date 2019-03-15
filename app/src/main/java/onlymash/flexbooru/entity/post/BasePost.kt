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

package onlymash.flexbooru.entity.post

import androidx.room.PrimaryKey
import com.crashlytics.android.Crashlytics

abstract class BasePost {
    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0L
    // to be consistent w/ changing backend order, we need to keep a data like this
    var indexInResponse: Int = -1
    var scheme: String = "http"
    var host: String = ""
    var keyword: String = ""

    internal fun checkUrl(url: String): String {
        var u = url
        if (u.contains("""\/""")) {
            u = u.replace("""\/""", "/")
        }
        return when {
            u.startsWith("http") -> u
            u.startsWith("//") -> "$scheme:$u"
            u.startsWith("/") -> "$scheme://$host$url"
            else -> {
                Crashlytics.log("Unknown url: $u")
                u
            }
        }
    }

    abstract fun getPostId(): Int

    abstract fun getPostWidth(): Int

    abstract fun getPostHeight(): Int

    abstract fun getPostScore(): Int

    abstract fun getPostRating(): String

    /**
     * return Preview url [String]
     * */
    abstract fun getPreviewUrl(): String

    /**
     * return Sample url [String]
     * */
    abstract fun getSampleUrl(): String

    /**
     * return Larger url [String]
     * */
    abstract fun getLargerUrl(): String

    /**
     * return Origin url [String]
     * */
    abstract fun getOriginUrl(): String

    abstract fun getCreatedDate(): String

    abstract fun getUpdatedDate(): String
}