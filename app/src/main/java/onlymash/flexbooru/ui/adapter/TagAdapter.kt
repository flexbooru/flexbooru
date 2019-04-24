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
import onlymash.flexbooru.entity.tag.*
import onlymash.flexbooru.ui.viewholder.TagViewHolder

class TagAdapter(private val listener: TagViewHolder.ItemListener,
                 retryCallback: () -> Unit) : BaseStatePagedListAdapter<TagBase, RecyclerView.ViewHolder>(TAG_COMPARATOR, retryCallback) {

    companion object {
        val TAG_COMPARATOR = object : DiffUtil.ItemCallback<TagBase>() {
            override fun areContentsTheSame(oldItem: TagBase, newItem: TagBase): Boolean =
                oldItem.getTagId() == newItem.getTagId() && oldItem.getTagName() == newItem.getTagName()
            override fun areItemsTheSame(oldItem: TagBase, newItem: TagBase): Boolean =
                oldItem.getTagId() == newItem.getTagId()
        }
    }

    override fun onCreateDataViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        TagViewHolder.create(parent)

    override fun onBindDataViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TagViewHolder) {
            holder.apply {
                setItemListener(listener)
                bind(getItem(position))
            }
        }
    }
}