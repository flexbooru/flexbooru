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

package onlymash.flexbooru.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.entity.pool.PoolDan
import onlymash.flexbooru.entity.pool.PoolDanOne
import onlymash.flexbooru.entity.pool.PoolMoe
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.ui.viewholder.PoolViewHolder

class PoolAdapter(private val glide: GlideRequests,
                  private val listener: PoolViewHolder.ItemListener,
                  retryCallback: () -> Unit
) : BaseStatePagedListAdapter<Any, RecyclerView.ViewHolder>(POOL_COMPARATOR, retryCallback) {

    companion object {
        val POOL_COMPARATOR = object : DiffUtil.ItemCallback<Any>() {
            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean = oldItem == newItem
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                return when {
                    oldItem is PoolDan && newItem is PoolDan -> oldItem.id == newItem.id
                    oldItem is PoolMoe && newItem is PoolMoe -> oldItem.id == newItem.id
                    oldItem is PoolDanOne && newItem is PoolDanOne -> oldItem.id == newItem.id
                    else -> false
                }
            }
        }
    }

    override fun onCreateDataViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        PoolViewHolder.create(parent, glide)

    override fun onBindDataViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PoolViewHolder) {
            holder.apply {
                setItemListener(listener)
                bind(getItem(position))
            }
        }
    }
}