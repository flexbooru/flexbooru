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
import onlymash.flexbooru.entity.post.BasePost
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.ui.viewholder.PostViewHolder

class PostAdapter(private val glide: GlideRequests,
                  private val listener: PostViewHolder.ItemListener,
                  private val showInfoBar: Boolean,
                  private val pageType: Int,
                  retryCallback: () -> Unit
) : BaseStatePagedListAdapter<BasePost, RecyclerView.ViewHolder>(POST_COMPARATOR, retryCallback) {

    companion object {
        val POST_COMPARATOR = object : DiffUtil.ItemCallback<BasePost>() {
            override fun areContentsTheSame(oldItem: BasePost, newItem: BasePost): Boolean =
                oldItem.getPostId() == newItem.getPostId() && oldItem.getPostScore() == newItem.getPostScore()
            override fun areItemsTheSame(oldItem: BasePost, newItem: BasePost): Boolean =
                oldItem.getPostId() == newItem.getPostId()
        }
    }

    override fun onCreateDataViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        PostViewHolder.create(parent, showInfoBar, glide)

    override fun onBindDataViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PostViewHolder) {
            holder.apply {
                bind(getItem(position), pageType)
                setItemListener(listener)
            }
        }
    }
}