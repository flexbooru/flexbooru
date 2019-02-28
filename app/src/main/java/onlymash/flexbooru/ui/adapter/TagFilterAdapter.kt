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
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.entity.TagFilter
import onlymash.flexbooru.ui.viewholder.TagFilterOrderViewHolder
import onlymash.flexbooru.ui.viewholder.TagFilterRatingViewHolder
import onlymash.flexbooru.ui.viewholder.TagFilterSubheadViewHolder
import onlymash.flexbooru.ui.viewholder.TagFilterViewHolder
import onlymash.flexbooru.widget.TagFilterView

class TagFilterAdapter(private val orders: Array<String>,
                       private val ratings: Array<String>,
                       private val list: RecyclerView) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_NORMAL = 1
        private const val VIEW_TYPE_ORDER_HEAD = 2
        private const val VIEW_TYPE_ORDER = 3
        private const val VIEW_TYPE_RATING_HEAD = 4
        private const val VIEW_TYPE_RATING = 5
    }

    private var orderSelected = ""
    private var ratingSelected = ""

    private var tags: MutableList<TagFilter> = mutableListOf()
    fun updateData(tags: MutableList<TagFilter>) {
        this.tags = tags
        notifyDataSetChanged()
    }
    fun getSelectedTagsString(): String {
        var str = ""
        tags.forEach {
            if (it.checked) {
                str = String.format("%s %s", it.name, str)
            }
        }
        if (!orderSelected.isBlank()) {
            str = "order:$orderSelected $str"
        }
        if (!ratingSelected.isBlank()) {
            str = "rating:$ratingSelected $str"
        }
        return str.trim()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_NORMAL -> TagFilterViewHolder.create(parent)
            VIEW_TYPE_ORDER -> TagFilterOrderViewHolder.create(parent)
            VIEW_TYPE_RATING -> TagFilterRatingViewHolder.create(parent)
            else -> TagFilterSubheadViewHolder.create(parent)
        }

    override fun getItemViewType(position: Int): Int {
        val orderHeadPos = tags.size
        val ratingHeadPos = orderHeadPos + orders.size + 1
        return when {
            position < orderHeadPos -> VIEW_TYPE_NORMAL
            position == ratingHeadPos -> VIEW_TYPE_RATING_HEAD
            position == orderHeadPos -> VIEW_TYPE_ORDER_HEAD
            position in (orderHeadPos + 1)..(ratingHeadPos - 1) -> VIEW_TYPE_ORDER
            else ->  VIEW_TYPE_RATING
        }
    }

    override fun getItemCount(): Int = tags.size + orders.size + ratings.size + 2

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TagFilterViewHolder -> holder.bind(tags[position])
            is TagFilterSubheadViewHolder -> {
                if (position == tags.size) {
                    holder.bind(holder.itemView.context.getString(R.string.order))
                } else {
                    holder.bind(holder.itemView.context.getString(R.string.rating))
                }
            }
            is TagFilterOrderViewHolder -> {
                val order = orders[position - tags.size - 1]
                holder.itemView.tag = order
                holder.bind(order) {
                    orderSelected = if (it) {
                        if (!orderSelected.isBlank()) {
                            list.findViewWithTag<TagFilterView>(orderSelected).apply {
                                animateCheckedAndInvoke(false) {}
                            }
                        }
                        order
                    } else ""
                }
            }
            is TagFilterRatingViewHolder -> {
                val rating = ratings[position - tags.size - orders.size - 2]
                holder.itemView.tag = rating
                holder.bind(rating) {
                    ratingSelected = if (it) {
                        if (!ratingSelected.isBlank()) {
                            list.findViewWithTag<TagFilterView>(ratingSelected).apply {
                                animateCheckedAndInvoke(false) {}
                            }
                        }
                        rating
                    } else ""
                }
            }
        }
    }
}