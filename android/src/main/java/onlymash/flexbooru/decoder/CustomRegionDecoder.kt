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

package onlymash.flexbooru.decoder

import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.core.net.toFile
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder
import java.io.FileInputStream

class CustomRegionDecoder : ImageRegionDecoder {

    private val decoderLock = Any()

    private var decoder: BitmapRegionDecoder? = null

    override fun isReady(): Boolean = !(decoder?.isRecycled ?: true)

    override fun init(context: Context?, uri: Uri): Point {
        val inputStream = FileInputStream(uri.toFile())
        decoder = BitmapRegionDecoder.newInstance(inputStream, false)
        inputStream.close()
        return Point(decoder!!.width, decoder!!.height)
    }

    override fun decodeRegion(sRect: Rect, sampleSize: Int): Bitmap {
        synchronized(decoderLock) {
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            return decoder?.decodeRegion(sRect, options)
                ?: throw RuntimeException("Region decoder returned null bitmap - image format may not be supported")
        }
    }

    override fun recycle() {
        decoder?.recycle()
    }

}