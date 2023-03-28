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

package onlymash.flexbooru.content

import coil.executeBlocking
import coil.imageLoader
import coil.request.ImageRequest
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider
import onlymash.flexbooru.worker.MuzeiArtWorker
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

class MuzeiProvider : MuzeiArtProvider() {

    override fun onLoadRequested(initial: Boolean) {
        MuzeiArtWorker.enqueueLoad()
    }

    @Throws(IOException::class)
    override fun openFile(artwork: Artwork): InputStream {
        val uri = artwork.persistentUri
        val context = context
        return if (context != null && uri != null && (uri.scheme == "http" || uri.scheme == "https")) {
            val key = uri.toString()
            val request = ImageRequest.Builder(context)
                .data(uri)
                .memoryCacheKey(key)
                .diskCacheKey(key)
                .build()
            context.imageLoader.executeBlocking(request)
            val file = context.imageLoader.diskCache?.get(key)?.data?.toFile()
            if (file != null && file.exists()) FileInputStream(file) else super.openFile(artwork)
        } else {
            super.openFile(artwork)
        }
    }
}