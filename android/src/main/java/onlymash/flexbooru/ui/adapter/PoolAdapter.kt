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

package onlymash.flexbooru.ui.adapter

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.app.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.data.model.common.Pool
import onlymash.flexbooru.databinding.ItemPoolBinding
import onlymash.flexbooru.extension.formatDate
import onlymash.flexbooru.extension.toggleArrow
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.ui.activity.AccountActivity
import onlymash.flexbooru.ui.activity.SearchActivity
import onlymash.flexbooru.ui.base.BasePagedListAdapter
import onlymash.flexbooru.ui.viewbinding.viewBinding
import onlymash.flexbooru.util.ViewAnimation
import onlymash.flexbooru.widget.LinkTransformationMethod

class PoolAdapter(
    private val glide: GlideRequests,
    private val downloadPoolCallback: (Int) -> Unit,
    retryCallback: () -> Unit
) : BasePagedListAdapter<Pool>(POOL_COMPARATOR, retryCallback) {
    companion object {
        val POOL_COMPARATOR = object : DiffUtil.ItemCallback<Pool>() {
            override fun areContentsTheSame(oldItem: Pool, newItem: Pool): Boolean =
                oldItem == newItem
            override fun areItemsTheSame(oldItem: Pool, newItem: Pool): Boolean =
                oldItem.id == newItem.id
        }
    }

    override fun onCreateItemViewHolder(
        parent: ViewGroup,
        viewType: Int): RecyclerView.ViewHolder = PoolViewHolder(parent)

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PoolViewHolder).apply {
            val pool = getItemSafe(position)
            bind(pool)
            itemView.setOnClickListener {
                pool?.let {
                    SearchActivity.startSearch(itemView.context, "pool:${it.id}")
                }
            }
            itemView.setOnLongClickListener {
                pool?.let {
                    downloadPoolCallback(it.id)
                }
                true
            }
            userAvatar.setOnClickListener {
                if (pool?.booruType == BOORU_TYPE_MOE || pool?.booruType == BOORU_TYPE_SANKAKU) {
                    itemView.context.startActivity(Intent(itemView.context, AccountActivity::class.java).apply {
                        putExtra(AccountActivity.USER_ID_KEY, pool.creatorId)
                        putExtra(AccountActivity.USER_NAME_KEY, pool.creatorName)
                        putExtra(AccountActivity.USER_AVATAR_KEY, pool.creatorAvatar)
                    })
                }
            }
        }
    }

    inner class PoolViewHolder(binding: ItemPoolBinding): RecyclerView.ViewHolder(binding.root) {

        constructor(parent: ViewGroup): this(parent.viewBinding(ItemPoolBinding::inflate))

        val userAvatar = binding.userAvatar
        private val poolName = binding.poolName
        private val poolIdCount = binding.poolIdAndCount
        private val poolDescription = binding.poolDescription
        private val poolDate = binding.poolDate
        private val expandBottom = binding.btExpand
        private val descriptionContainer = binding.descriptionContainer
        private var isShowing = false


        init {
            expandBottom.setOnClickListener {
                if (!poolDescription.text.isNullOrBlank()) {
                    isShowing = toggleLayoutExpand(!isShowing, expandBottom, descriptionContainer)
                }
            }
            poolDescription.transformationMethod = LinkTransformationMethod()

        }

        fun bind(pool: Pool?) {
            if (descriptionContainer.visibility == View.VISIBLE) {
                isShowing = false
                expandBottom.toggleArrow(show = false, delay = false)
                descriptionContainer.isVisible = false
            }
            if (pool == null) return
            val context = itemView.context
            poolName.text = pool.name
            poolIdCount.text = String.format(context.getString(R.string.pool_info_id_and_count), pool.id, pool.count)
            poolDescription.text = pool.description
            expandBottom.isVisible = pool.description.isNotBlank()
            poolDate.text = itemView.context.formatDate(pool.time)
            when (pool.booruType) {
                BOORU_TYPE_MOE -> {
                    glide.load(String.format(context.getString(R.string.account_user_avatars), pool.scheme, pool.host, pool.creatorId))
                        .placeholder(ResourcesCompat.getDrawable(context.resources, R.drawable.avatar_account, context.theme))
                        .into(userAvatar)
                }
                BOORU_TYPE_SANKAKU -> {
                    glide.load(pool.creatorAvatar)
                        .placeholder(ResourcesCompat.getDrawable(context.resources, R.drawable.avatar_account, context.theme))
                        .into(userAvatar)
                }
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
}