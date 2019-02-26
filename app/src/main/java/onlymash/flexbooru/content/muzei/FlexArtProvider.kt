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

package onlymash.flexbooru.content.muzei

import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import com.google.android.apps.muzei.api.UserCommand
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider
import onlymash.flexbooru.R
import onlymash.flexbooru.api.DownloadUtil
import java.io.IOException
import java.io.InputStream
import java.net.URL

class FlexArtProvider : MuzeiArtProvider() {
    companion object {
        private const val TAG = "FlexArtProvider"
        private const val COMMAND_ID_VIEW_PROFILE = 1
        private const val COMMAND_ID_VISIT_APP = 2
    }
    override fun onLoadRequested(initial: Boolean) {
        FlexArtWorker.enqueueLoad()
    }

    override fun getCommands(artwork: Artwork): MutableList<UserCommand> =
        context?.run {
            listOf(
                UserCommand(
                    COMMAND_ID_VIEW_PROFILE,
                    getString(R.string.muzei_action_view_profile, artwork.byline)),
                UserCommand(
                    COMMAND_ID_VISIT_APP,
                    getString(R.string.muzei_action_visit_flexbooru)))
        } as MutableList<UserCommand>? ?: super.getCommands(artwork)

    override fun onCommand(artwork: Artwork, id: Int) {
        val context = context ?: return
        when (id) {
            COMMAND_ID_VIEW_PROFILE -> {
                val profileUri = artwork.metadata?.toUri() ?: return
                context.startActivity(Intent(Intent.ACTION_VIEW, profileUri))
            }
            COMMAND_ID_VISIT_APP -> {

            }
        }
    }

    @Throws(IOException::class)
    override fun openFile(artwork: Artwork): InputStream {
        artwork.webUri?.takeIf {
            it.scheme == "http" || it.scheme == "https"
        }?.run {
            val response = DownloadUtil.create().trackDownload(toString()).execute()
            return if(response.isSuccessful) {
                response.body()?.byteStream() ?: throw IOException("Unable to open stream for $this")
            } else {
                super.openFile(artwork)
            }
        } ?: return super.openFile(artwork)
    }
}