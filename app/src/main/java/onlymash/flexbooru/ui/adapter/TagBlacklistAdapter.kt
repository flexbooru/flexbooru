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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.entity.TagBlacklist
import onlymash.flexbooru.ui.viewholder.TagBlacklistViewHolder

class TagBlacklistAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var data: MutableList<TagBlacklist> = mutableListOf()

    fun updateData(data: MutableList<TagBlacklist>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        TagBlacklistViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tag_blacklist, parent, false))

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as TagBlacklistViewHolder).bind(data[position])
    }
}