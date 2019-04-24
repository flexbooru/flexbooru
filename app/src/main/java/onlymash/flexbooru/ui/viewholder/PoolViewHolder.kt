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

package onlymash.flexbooru.ui.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import onlymash.flexbooru.R
import onlymash.flexbooru.entity.pool.PoolBase
import onlymash.flexbooru.entity.pool.PoolMoe
import onlymash.flexbooru.entity.pool.PoolSankaku
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.util.ViewAnimation
import onlymash.flexbooru.util.toggleArrow
import onlymash.flexbooru.widget.AutoCollapseTextView
import onlymash.flexbooru.widget.CircularImageView
import onlymash.flexbooru.widget.LinkTransformationMethod

class PoolViewHolder(itemView: View, private val glide: GlideRequests): RecyclerView.ViewHolder(itemView) {

    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests): PoolViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_pool, parent, false)
            return PoolViewHolder(view, glide)
        }
    }
    private val container: ConstraintLayout = itemView.findViewById(R.id.container)
    private val userAvatar: CircularImageView = itemView.findViewById(R.id.user_avatar)
    private val poolName: TextView = itemView.findViewById(R.id.pool_name)
    private val poolIdCount: TextView = itemView.findViewById(R.id.pool_id_and_count)
    private val poolDescription: AutoCollapseTextView = itemView.findViewById(R.id.pool_description)
    private val poolDate: TextView = itemView.findViewById(R.id.pool_date)
    private val expandBottom: ImageButton = itemView.findViewById(R.id.bt_expand)
    private val descriptionContainer: LinearLayout = itemView.findViewById(R.id.description_container)
    private var pool: PoolBase? = null
    private var isShowing = false

    private var itemListener: ItemListener? = null

    fun setItemListener(listener: ItemListener) {
        itemListener = listener
    }

    interface ItemListener {
        fun onClickItem(keyword: String)
        fun onClickUserAvatar(id: Int, name: String?, avatar: String? = null)
    }

    init {
        container.setOnClickListener {
            val id = pool?.getPoolId() ?: return@setOnClickListener
            itemListener?.onClickItem("pool:$id")
        }
        expandBottom.setOnClickListener {
            if (!poolDescription.text.isNullOrBlank()) {
                isShowing = toggleLayoutExpand(!isShowing, expandBottom, descriptionContainer)
            } else {
                Snackbar.make(container, container.context.getString(R.string.pool_description_is_empty),
                    Snackbar.LENGTH_SHORT).show()
            }
        }
        poolDescription.transformationMethod = LinkTransformationMethod()
        userAvatar.setOnClickListener {
            pool?.let {
                if (it is PoolSankaku) {
                    itemListener?.onClickUserAvatar(it.getCreatorId(), it.getCreatorName(), it.author.avatar)
                } else {
                    itemListener?.onClickUserAvatar(it.getCreatorId(), it.getCreatorName())
                }
            }
        }
    }

    fun bind(data: PoolBase?) {
        if (descriptionContainer.visibility == View.VISIBLE) {
            isShowing = false
            expandBottom.toggleArrow(show = false, delay = false)
            descriptionContainer.visibility = View.GONE
        }
        pool = data ?: return
        val res = container.context.resources
        poolName.text = data.getPoolName()
        poolIdCount.text = String.format(res.getString(R.string.pool_info_id_and_count), data.getPoolId(), data.getPostCount())
        poolDescription.text = data.getPoolDescription()
        poolDate.text = data.getPoolDate()
        when (data) {
            is PoolMoe -> {
                glide.load(String.format(res.getString(R.string.account_user_avatars), data.scheme, data.host, data.getCreatorId()))
                    .placeholder(res.getDrawable(R.drawable.avatar_account, container.context.theme))
                    .into(userAvatar)
            }
            is PoolSankaku -> {
                glide.load(data.author.avatar)
                    .placeholder(res.getDrawable(R.drawable.avatar_account, container.context.theme))
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