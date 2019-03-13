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
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.entity.comment.CommentMoe
import onlymash.flexbooru.entity.User
import onlymash.flexbooru.entity.comment.CommentDan
import onlymash.flexbooru.entity.comment.CommentDanOne
import onlymash.flexbooru.entity.comment.CommentGel
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.repository.NetworkState
import onlymash.flexbooru.ui.viewholder.CommentViewHolder
import onlymash.flexbooru.ui.viewholder.NetworkStateViewHolder

class CommentAdapter(private val glide: GlideRequests,
                     private val user: User?,
                     private val listener: CommentViewHolder.Listener,
                     private val retryCallback: () -> Unit) : PagedListAdapter<Any, RecyclerView.ViewHolder>(COMMENT_COMPARATOR) {


    private var networkState: NetworkState? = null

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    override fun getItemCount(): Int = super.getItemCount() + if (hasExtraRow()) 1 else 0

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            R.layout.item_network_state
        } else {
            super.getItemViewType(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.item_network_state -> NetworkStateViewHolder.create(parent, retryCallback)
            else -> CommentViewHolder.create(parent, glide, user, listener)
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == R.layout.item_network_state) {
            (holder as NetworkStateViewHolder).bindTo(networkState)
        } else {
            (holder as CommentViewHolder).bind(getItem(position))
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

    companion object {
        val COMMENT_COMPARATOR = object : DiffUtil.ItemCallback<Any>() {
            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean = oldItem == newItem
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                return when {
                    oldItem is CommentMoe && newItem is CommentMoe -> oldItem.id == newItem.id
                    oldItem is CommentDan && newItem is CommentDan -> oldItem.id == newItem.id
                    oldItem is CommentDanOne && newItem is CommentDanOne -> oldItem.id == newItem.id
                    oldItem is CommentGel && newItem is CommentGel -> oldItem.id == newItem.id
                    else -> false
                }
            }
        }
    }
}