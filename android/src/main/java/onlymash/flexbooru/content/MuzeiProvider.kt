/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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

import android.content.Intent
import com.google.android.apps.muzei.api.UserCommand
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Keys.POST_QUERY
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.ui.activity.SearchActivity
import onlymash.flexbooru.worker.MuzeiArtWorker
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

class MuzeiProvider : MuzeiArtProvider() {
    companion object {
        private const val COMMAND_ID_VIEW_POST = 1
        private const val COMMAND_ID_SEARCH_POSTS = 2
    }
    override fun onLoadRequested(initial: Boolean) {
        MuzeiArtWorker.enqueueLoad()
    }

    override fun getCommands(artwork: Artwork): MutableList<UserCommand> =
        context?.run {
            listOf(
                UserCommand(COMMAND_ID_VIEW_POST, getString(R.string.muzei_action_view_post)),
                UserCommand(COMMAND_ID_SEARCH_POSTS, getString(R.string.muzei_action_search_posts)))
        } as MutableList<UserCommand>? ?: super.getCommands(artwork)

    override fun onCommand(artwork: Artwork, id: Int) {
        val context = context ?: return
        when (id) {
            COMMAND_ID_VIEW_POST -> {
                val query = artwork.token ?: return
                context.startActivity(
                    Intent(context, SearchActivity::class.java)
                        .putExtra(POST_QUERY, query)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            COMMAND_ID_SEARCH_POSTS -> {
                val query = artwork.byline ?: return
                context.startActivity(
                    Intent(context, SearchActivity::class.java)
                        .putExtra(POST_QUERY, query)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
    }

    @Throws(IOException::class)
    override fun openFile(artwork: Artwork): InputStream {
        val uri = artwork.persistentUri
        val context = context
        return if (context != null && uri != null && (uri.scheme == "http" || uri.scheme == "https")) {
            val file =
                GlideApp.with(context)
                    .downloadOnly()
                    .load(uri)
                    .submit()
                    .get()
            if (file != null && file.exists()) FileInputStream(file) else super.openFile(artwork)
        } else {
            super.openFile(artwork)
        }
    }
}