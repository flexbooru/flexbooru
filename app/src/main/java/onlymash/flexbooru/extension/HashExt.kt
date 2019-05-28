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

package onlymash.flexbooru.extension

import java.security.MessageDigest

/**
 * Supported algorithms on Android:
 *
 * Algorithm	Supported API Levels
 * MD5          1+
 * SHA-1	    1+
 * SHA-224	    1-8,22+
 * SHA-256	    1+
 * SHA-384	    1+
 * SHA-512	    1+
 */

private fun String.hashString(type: String): String {
    val hexChars = "0123456789abcdef"
    val bytes = MessageDigest
        .getInstance(type)
        .digest(toByteArray())
    val result = StringBuilder(bytes.size * 2)

    bytes.forEach {
        val i = it.toInt()
        result.append(hexChars[i shr 4 and 0x0f])
        result.append(hexChars[i and 0x0f])
    }

    return result.toString()
}

fun String.sha512() = hashString("SHA-512")

fun String.sha256() = hashString("SHA-256")

fun String.sha1() = hashString("SHA-1")

fun String.md5() = hashString("MD5")