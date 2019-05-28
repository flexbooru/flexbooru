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

import java.io.*
import java.nio.charset.Charset

private const val EOF = -1
private const val DEFAULT_BUFFER_SIZE = 1024 * 8

private val UTF_8 = Charset.forName("UTF-8")

fun Closeable.safeCloseQuietly() {
    try {
        close()
    } catch (_: IOException) {
        // Ignore
    }

}

@Throws(IOException::class)
fun InputStream.copyToOS(os: OutputStream?): Long {
    if (os == null) return 0
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var count: Long = 0
    var n: Int = read(buffer)
    while (n != EOF) {
        os.write(buffer, 0, n)
        count += n.toLong()
        n = read(buffer)
    }
    return count
}

@Throws(IOException::class)
fun InputStream.toString(charsetName: String?): String? {
    if (charsetName == null) return null
    val os = ByteArrayOutputStream()
    copyToOS(os)
    return os.toString(charsetName)
}

@Throws(IOException::class)
fun InputStream.toByteArray(): ByteArray? {
    val os = ByteArrayOutputStream()
    copyToOS(os)
    return os.toByteArray()
}