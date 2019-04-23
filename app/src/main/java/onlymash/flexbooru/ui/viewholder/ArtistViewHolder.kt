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

package onlymash.flexbooru.ui.viewholder

import android.content.ClipData
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import onlymash.flexbooru.App
import onlymash.flexbooru.R
import onlymash.flexbooru.entity.artist.ArtistBase
import onlymash.flexbooru.util.ViewAnimation
import onlymash.flexbooru.util.toggleArrow
import onlymash.flexbooru.widget.LinkTransformationMethod

class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun create(parent: ViewGroup): ArtistViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_artist, parent, false)
            return ArtistViewHolder(view)
        }
    }

    private val artistName: TextView = itemView.findViewById(R.id.artist_name)
    private val artistId: TextView = itemView.findViewById(R.id.artist_id)
    private val btExpand: ImageButton = itemView.findViewById(R.id.bt_expand)
    private val urlsContainer: LinearLayout = itemView.findViewById(R.id.urls_container)
    private val artistUrls: TextView = itemView.findViewById(R.id.artist_urls)
    private var isShowing = false
    private var artist: ArtistBase? = null

    init {
        itemView.setOnClickListener {
            artist?.let {
                itemListener?.onClickItem(it.getArtistName())
            }
        }
        itemView.setOnLongClickListener {
            val text = artistName.text
            if (!text.isNullOrBlank()) {
                App.app.clipboard.primaryClip = ClipData.newPlainText("Artist", text)
            }
            true
        }
        btExpand.setOnClickListener {
            if (!artistUrls.text.isNullOrBlank()) {
                isShowing = toggleLayoutExpand(!isShowing, btExpand, urlsContainer)
            } else {
                Snackbar.make(itemView, itemView.context.getString(R.string.artist_urls_is_empty),
                    Snackbar.LENGTH_SHORT).show()
            }
        }
        artistUrls.transformationMethod = LinkTransformationMethod()
    }

    private var itemListener: ItemListener? = null

    fun setItemListener(listener: ItemListener) {
        itemListener = listener
    }

    interface ItemListener {
        fun onClickItem(keyword: String)
    }

    fun bind(data: ArtistBase?) {
        artist = data
        if (urlsContainer.visibility == View.VISIBLE) {
            isShowing = false
            btExpand.toggleArrow(show = false, delay = false)
            urlsContainer.visibility = View.GONE
        }
        if (data == null) return
        artistName.text = data.getArtistName()
        artistId.text = String.format("#%d", data.getArtistId())
        var urlsText = ""
        data.getArtistUrls().forEach { url ->
            urlsText = String.format("%s\r\n%s", url, urlsText)
        }
        if (urlsText.isNotBlank()) {
            artistUrls.text = urlsText
        }
    }

    private fun toggleLayoutExpand(show: Boolean, view: View, container: View): Boolean {
        view.toggleArrow(show)
        if (show)
            ViewAnimation.expand(container)
        else
            ViewAnimation.collapse(container)
        return show
    }
}