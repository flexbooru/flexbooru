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

package onlymash.flexbooru.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.entity.artist.ArtistBase
import onlymash.flexbooru.ui.viewholder.ArtistViewHolder

class ArtistAdapter(private val listener: ArtistViewHolder.ItemListener,
                    retryCallback: () -> Unit
) : BaseStatePagedListAdapter<ArtistBase, RecyclerView.ViewHolder>(ARTIST_COMPARATOR, retryCallback) {

    companion object {
        val ARTIST_COMPARATOR = object : DiffUtil.ItemCallback<ArtistBase>() {
            override fun areContentsTheSame(oldItem: ArtistBase, newItem: ArtistBase): Boolean =
                oldItem.getArtistId() == newItem.getArtistId() && oldItem.getArtistName() == newItem.getArtistName()
            override fun areItemsTheSame(oldItem: ArtistBase, newItem: ArtistBase): Boolean {
                return oldItem.getArtistId() == newItem.getArtistId()
            }
        }
    }

    override fun onCreateDataViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ArtistViewHolder.create(parent)

    override fun onBindDataViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ArtistViewHolder) {
            holder.apply {
                setItemListener(listener)
                bind(getItem(position))
            }
        }
    }
}