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

package onlymash.flexbooru.util.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso

class CustomDecoder(private val picasso: Picasso) : ImageDecoder {
    override fun decode(context: Context?, uri: Uri): Bitmap {
        return picasso.load(uri.toFile())
            .config(Bitmap.Config.ARGB_8888)
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .get()
    }
}