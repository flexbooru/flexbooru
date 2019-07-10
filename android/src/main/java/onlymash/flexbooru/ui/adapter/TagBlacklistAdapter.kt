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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.entity.common.TagBlacklist
import onlymash.flexbooru.ui.viewholder.TagBlacklistViewHolder

class TagBlacklistAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var data: MutableList<TagBlacklist> = mutableListOf()

    fun updateData(data: MutableList<TagBlacklist>) {
        val tmpData: MutableList<TagBlacklist> = mutableListOf()
        tmpData.addAll(this.data)
        this.data = data
        val diffResult = DiffUtil.calculateDiff(TagBlacklistAdapterDiffCallback(tmpData, this.data))
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        TagBlacklistViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tag_blacklist, parent, false))

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as TagBlacklistViewHolder).bind(data[position])
    }

    inner class TagBlacklistAdapterDiffCallback(private val oldTagBlacklists: MutableList<TagBlacklist>,
                                                private val newTagBlacklists: MutableList<TagBlacklist>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldTagBlacklists[oldItemPosition].uid == newTagBlacklists[newItemPosition].uid

        override fun getOldListSize(): Int = oldTagBlacklists.size

        override fun getNewListSize(): Int = newTagBlacklists.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldTagBlacklists[oldItemPosition] == newTagBlacklists[newItemPosition]

    }
}