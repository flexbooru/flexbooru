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

package onlymash.flexbooru.okhttp

import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*
import java.io.IOException

class ProgressResponseBody(
    private val responseBody: ResponseBody?,
    private val progressListener: ProgressListener
) : ResponseBody() {


    private var bufferedSource: BufferedSource? = null

    override fun contentType(): MediaType? {
        return responseBody?.contentType()
    }

    @Throws(IOException::class)
    override fun contentLength(): Long {
        return responseBody?.contentLength() ?: 1L
    }

    @Throws(IOException::class)
    override fun source(): BufferedSource {
        if (bufferedSource == null) {
            bufferedSource = responseBody?.let { source(it.source()).buffer() }
        }
        return bufferedSource ?: throw IllegalStateException("source is null")
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L
            @Throws(IOException::class)
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                responseBody?.let {
                    progressListener.onUpdate(
                        totalBytesRead,
                        it.contentLength(),
                        bytesRead == -1L
                    )
                }
                return bytesRead
            }
        }
    }
}
