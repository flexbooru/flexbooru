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
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.database.TagFilterManager
import onlymash.flexbooru.entity.TagFilter
import onlymash.flexbooru.ui.viewholder.TagFilterOrderViewHolder
import onlymash.flexbooru.ui.viewholder.TagFilterRatingViewHolder
import onlymash.flexbooru.ui.viewholder.TagFilterSubheadViewHolder
import onlymash.flexbooru.ui.viewholder.TagFilterViewHolder
import onlymash.flexbooru.widget.TagFilterView

class TagFilterAdapter(private val orders: Array<String>,
                       private val ratings: Array<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_NORMAL = 1
        private const val VIEW_TYPE_ORDER_HEAD = 2
        private const val VIEW_TYPE_ORDER = 3
        private const val VIEW_TYPE_RATING_HEAD = 4
        private const val VIEW_TYPE_RATING = 5
    }

    private var orderSelected = ""
    private var ratingSelected = ""
    private val tagsSelected: MutableList<String> = mutableListOf()

    private var refreshingOrder = false
    private var refreshingRating = false

    private fun refreshOrder(order: String) {
        val index = orders.indexOfFirst { it == order }
        refreshingOrder = true
        notifyItemChanged(tags.size + index + 1)
    }

    private fun refreshRating(rating: String) {
        val index = ratings.indexOfFirst { it == rating }
        refreshingRating = true
        notifyItemChanged(tags.size + orders.size + index + 2)
    }

    private var tags: MutableList<TagFilter> = mutableListOf()
    fun updateData(tags: MutableList<TagFilter>) {
        this.tags = tags
        notifyDataSetChanged()
    }
    fun getSelectedTagsString(): String {
        var str = ""
        tagsSelected.forEach {
            str = String.format("%s %s", it, str)
        }
        str = str.trim()
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
            is TagFilterViewHolder -> {
                val tag = tags[position]
                val name = tag.name
                (holder.itemView as TagFilterView).apply {
                    text = name
                    isChecked = tagsSelected.contains(name)
                    setOnClickListener {
                        if (tagsSelected.contains(name)) {
                            animateCheckedAndInvoke(false) {
                                tagsSelected.remove(name)
                            }
                        } else {
                            animateCheckedAndInvoke(true) {
                                tagsSelected.add(name)
                            }
                        }
                    }
                    setOnLongClickListener {
                        val context = holder.itemView.context ?: return@setOnLongClickListener true
                        AlertDialog.Builder(context)
                            .setTitle(String.format(context.getString(R.string.tag_delete_title), name))
                            .setPositiveButton(R.string.dialog_yes) { _, _ ->
                                TagFilterManager.deleteTagFilter(tag)
                            }
                            .setNegativeButton(R.string.dialog_no, null)
                            .create()
                            .show()
                        true
                    }
                }
            }
            is TagFilterSubheadViewHolder -> {
                if (position == tags.size) {
                    holder.bind(holder.itemView.context.getString(R.string.order))
                } else {
                    holder.bind(holder.itemView.context.getString(R.string.rating))
                }
            }
            is TagFilterOrderViewHolder -> {
                val order = orders[position - tags.size - 1]
                val checkedState = order == orderSelected
                (holder.itemView as TagFilterView).apply {
                    text = order
                    tag = order
                    if (refreshingOrder) {
                        refreshingOrder = false
                        animateCheckedAndInvoke(checkedState) {}
                    } else {
                        isChecked = checkedState
                    }
                    setOnClickListener {
                        val checked = !isChecked
                        animateCheckedAndInvoke(checked) {}
                        val oldSelected = orderSelected
                        orderSelected = if (checked) order else ""
                        if (oldSelected.isNotEmpty()) refreshOrder(oldSelected)
                    }
                }
            }
            is TagFilterRatingViewHolder -> {
                val rating = ratings[position - tags.size - orders.size - 2]
                val checkedState = rating == ratingSelected
                (holder.itemView as TagFilterView).apply {
                    text = rating
                    tag = rating
                    if (refreshingRating) {
                        refreshingRating = false
                        animateCheckedAndInvoke(checkedState) {}
                    } else {
                        isChecked = checkedState
                    }
                    setOnClickListener {
                        val checked = !isChecked
                        animateCheckedAndInvoke(checked) {}
                        val oldSelected = ratingSelected
                        ratingSelected = if (checked) rating else ""
                        if (oldSelected.isNotEmpty()) refreshRating(oldSelected)
                    }
                }
            }
        }
    }
}