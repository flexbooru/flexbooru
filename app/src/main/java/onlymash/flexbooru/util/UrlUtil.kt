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

package onlymash.flexbooru.util

import java.util.regex.Pattern

object UrlUtil {

    private val pattern = Pattern.compile("\\S*[?]\\S*")

    /**
     *
     * @return 链接扩展名
     */
    fun parseSuffix(url: String): String {

        val matcher = pattern.matcher(url)

        val spUrl = url.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val len = spUrl.size
        val endUrl = spUrl[len - 1]

        if (matcher.find()) {
            val spEndUrl = endUrl.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return spEndUrl[0].split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        }
        return endUrl.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
    }

    fun isMP4(url: String): Boolean {
        val suffix = parseSuffix(url)
        return suffix == "mp4" || suffix == "MP4"
    }

    fun isImage(url: String): Boolean {
        val suffix = parseSuffix(url)
        return suffix == "jpg" ||
                suffix == "JPG" ||
                suffix == "png" ||
                suffix == "PNG" ||
                suffix == "jpeg" ||
                suffix == "JPEG"
    }
}

fun String.isImage(): Boolean {
    val ext = ext()
    return ext == "jpg" || ext == "png" || ext == "gif" ||
            ext == "webp"
}
fun String.isStillImage(): Boolean {
    val ext = ext()
    return ext == "jpg" || ext == "png"
}
fun String.isGifImage(): Boolean = ext() == "gif"
fun String.isImageNotWebp(): Boolean {
    val ext = ext()
    return ext == "jpg" || ext == "png" || ext == "gif"
}
fun String.isWebp(): Boolean = ext() == "webp"

fun String.ext(): String {
    val start = lastIndexOf('.') + 1
    val end = indexOfFirst { it == '?' }
    return if (end > start) {
        substring(start, end)
    } else {
        substring(start)
    }
}