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
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import onlymash.flexbooru.R
import onlymash.flexbooru.repository.NetworkState
import onlymash.flexbooru.ui.viewholder.HeaderViewHolder
import onlymash.flexbooru.ui.viewholder.NetworkStateViewHolder

abstract class BaseStatePagedListAdapter<T, VH : RecyclerView.ViewHolder>(
    diffCallback: DiffUtil.ItemCallback<T>,
    private val retryCallback: () -> Unit
) : BaseHeaderPagedListAdapter<T, VH>(diffCallback) {

    abstract fun onCreateDataViewHolder(parent: ViewGroup, viewType: Int): VH

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return when (viewType) {
            R.layout.item_header -> HeaderViewHolder.create(parent) as VH
            R.layout.item_network_state -> NetworkStateViewHolder.create(parent, retryCallback) as VH
            else -> onCreateDataViewHolder(parent, viewType)
        }
    }

    abstract fun onBindDataViewHolder(holder: VH, position: Int)

    override fun onBindViewHolder(holder: VH, position: Int) {
        when (getItemViewType(position)) {
            R.layout.item_header -> {
                if (holder.itemView.layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                    (holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
                }
            }
            R.layout.item_network_state -> {
                if (holder.itemView.layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                    (holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
                }
                (holder as NetworkStateViewHolder).bindTo(networkState)
            }
            else -> {
                onBindDataViewHolder(holder, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    private var networkState: NetworkState? = null

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            R.layout.item_header
        } else if (hasExtraRow() && position == itemCount - 1) {
            R.layout.item_network_state
        } else {
            super.getItemViewType(position)
        }
    }

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }
}