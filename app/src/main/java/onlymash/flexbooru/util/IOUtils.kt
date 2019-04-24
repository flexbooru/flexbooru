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

package onlymash.flexbooru.util

import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

object IOUtils {

    private const val EOF = -1
    private const val DEFAULT_BUFFER_SIZE = 1024 * 8

    /**
     * UTF_8 Charset.
     */
    private val UTF_8 = Charset.forName("UTF-8")

    /**
     * Closes the [Closeable]. Have no worries.
     *
     * @param closeable the objects to close, may be null or already closed
     */
    fun closeQuietly(closeable: Closeable?) {
        try {
            closeable?.close()
        } catch (_: IOException) {
            // Ignore
        }

    }

    /**
     * Copies all bytes from `is` to `os`.
     *
     * @param is the `InputStream` to read from, may be null
     * @param os the `OutputStream` to write to, may be null
     * @return the number of bytes copied, or 0 if `is` or `os` is null
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun copy(`is`: InputStream?, os: OutputStream?): Long {
        if (`is` == null || os == null) return 0
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var count: Long = 0
        var n: Int = `is`.read(buffer)
        while (n != EOF) {
            os.write(buffer, 0, n)
            count += n.toLong()
            n = `is`.read(buffer)
        }
        return count
    }

    /**
     * Reads all bytes from `is` as a [String].
     *
     * @param is the `InputStream` to read from, may be null
     * @param charset the charset of the requested String, may be null
     * @return the requested String, or `null` if `is` or `charset` is null
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    @JvmOverloads
    fun toString(`is`: InputStream?, charset: Charset? = UTF_8): String? {
        return if (`is` == null || charset == null) null else toString(`is`, charset.name())
    }

    /**
     * Reads all bytes from `is` as a [String].
     *
     * @param is the `InputStream` to read from, may be null
     * @param charsetName the charset name of the requested String, may be null
     * @return the requested String, or `null` if `is` or `charsetName` is null
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun toString(`is`: InputStream?, charsetName: String?): String? {
        if (`is` == null || charsetName == null) return null
        val os = ByteArrayOutputStream()
        copy(`is`, os)
        return os.toString(charsetName)
    }

    /**
     * Reads all bytes from `is`.
     *
     * @param is the `InputStream` to read from, may be null
     * @return the requested byte array, or `null` if `is` is null
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun toByteArray(`is`: InputStream?): ByteArray? {
        if (`is` == null) return null
        val os = ByteArrayOutputStream()
        copy(`is`, os)
        return os.toByteArray()
    }
}