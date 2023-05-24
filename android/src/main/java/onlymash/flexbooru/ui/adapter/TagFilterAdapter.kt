/*
 * Copyright (C) 2020. by onlymash <fiepi.dev@gmail.com>, All rights reserved
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

import android.content.res.ColorStateList
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Settings.isShowAllTags
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL_LEGACY
import onlymash.flexbooru.app.Values.BOORU_TYPE_UNKNOWN
import onlymash.flexbooru.data.model.common.TagFilter
import onlymash.flexbooru.databinding.ItemTagFilterSubheadBinding
import onlymash.flexbooru.ui.viewbinding.viewBinding
import onlymash.flexbooru.ui.base.BaseTagFilterViewHolder

class TagFilterAdapter(private val deleteTagCallback: (TagFilter) -> Unit,
                       private val addSearchBarTextCallback: () -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ADD = 1
        private const val VIEW_TYPE_NORMAL = 2
        private const val VIEW_TYPE_ORDER_HEAD = 3
        private const val VIEW_TYPE_ORDER = 4
        private const val VIEW_TYPE_RATING_HEAD = 5
        private const val VIEW_TYPE_RATING = 6
        private const val VIEW_TYPE_THRESHOLD_HEAD = 7
        private const val VIEW_TYPE_THRESHOLD = 8

        fun tagFilterDiffCallback(oldTags: List<TagFilter>, newTags: List<TagFilter>) = object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldTags[oldItemPosition].uid == newTags[newItemPosition].uid

            override fun getOldListSize(): Int = oldTags.size

            override fun getNewListSize(): Int = newTags.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldTags[oldItemPosition].name == newTags[newItemPosition].name
        }
    }

    private var ratings: MutableList<String> = mutableListOf()
    private var orders: MutableList<String> = mutableListOf()
    private var thresholds: MutableList<String> = mutableListOf()
    private var booruType: Int = BOORU_TYPE_UNKNOWN
    private var orderSelected = ""
    private var ratingSelected = ""
    private var thresholdSelected = ""
    private val tagsSelected: MutableList<TagFilter> = mutableListOf()
    private var booruUid: Long = -1
    var isShowAll = isShowAllTags
        set(value) {
            field = value
            if (!value) {
                tagsSelected.clear()
                refreshData(reset = true)
            } else {
                refreshData()
            }
        }

    private var tags: MutableList<TagFilter> = mutableListOf()
    private var allTags: MutableList<TagFilter> = mutableListOf()

    private val listUpdateCallback = object : ListUpdateCallback {

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            notifyItemRangeChanged(position + 1, count, payload)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            notifyItemMoved(fromPosition + 1, toPosition + 1)
        }

        override fun onInserted(position: Int, count: Int) {
            notifyItemRangeInserted(position + 1, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            notifyItemRangeRemoved(position + 1, count)
        }
    }

    fun updateBooru(
        booruUid: Long,
        booruType: Int,
        ratings: Array<String>,
        orders: Array<String>,
        thresholds: Array<String>
    ) {
        this.booruUid = booruUid
        this.booruType = booruType
        this.ratings.apply {
            clear()
            addAll(ratings)
        }
        this.orders.apply {
            clear()
            addAll(orders)
        }
        this.thresholds.apply {
            clear()
            addAll(thresholds)
        }
        refreshData(reset = true)
    }

    private fun refreshOrder(order: String) {
        val index = orders.indexOfFirst { it == order }
        notifyItemChanged(tags.size + index + 2)
    }

    private fun refreshRating(rating: String) {
        val index = ratings.indexOfFirst { it == rating }
        notifyItemChanged(tags.size + orders.size + index + 3)
    }

    private fun refreshThreshold(threshold: String) {
        val index = thresholds.indexOfFirst { it == threshold }
        notifyItemChanged(tags.size + orders.size + ratings.size + index + 4)
    }

    fun updateData(newTags: List<TagFilter>) {
        allTags.clear()
        allTags.addAll(newTags)
        refreshData()
    }

    private fun refreshData(reset: Boolean = false) {
        if (booruType == BOORU_TYPE_UNKNOWN) {
            notifyDataSetChanged()
            return
        }
        val oldTags: MutableList<TagFilter> = mutableListOf()
        oldTags.addAll(tags)
        tags.clear()
        if (isShowAll) {
            tags.addAll(allTags)
        } else {
            allTags.forEach {
                if (it.booruUid == booruUid) {
                    tags.add(it)
                }
            }
        }
        if (reset) {
            notifyDataSetChanged()
        } else {
            val diffResult = DiffUtil.calculateDiff(tagFilterDiffCallback(oldTags, tags))
            diffResult.dispatchUpdatesTo(listUpdateCallback)
        }
    }

    fun getSelectedTagsString(): String {
        // `tagsSelected` may contains removed tags
        val removedTags = tagsSelected.toSet() - allTags.toSet()
        tagsSelected.removeAll(removedTags)

        // only tags of the current booru should be selected
        val currentBooruTagsSelected = if (isShowAll) {
            tagsSelected
        } else {
            tagsSelected.filter { it.booruUid == booruUid }
        }

        // convert tag objects to tag strings and remove duplications
        val result: MutableList<String> = currentBooruTagsSelected.map { it.name }.toSet().toMutableList()

        // append special tags
        if (orderSelected.isNotEmpty()) {
            if (booruType == BOORU_TYPE_GEL || booruType == BOORU_TYPE_GEL_LEGACY) {
                result.add("sort:$orderSelected")
            } else {
                result.add("order:$orderSelected")
            }
        }
        if (ratingSelected.isNotEmpty()) {
            result.add("rating:$ratingSelected")
        }
        if (thresholdSelected.isNotEmpty()) {
            result.add("threshold:$thresholdSelected")
        }

        return result.joinToString(" ").trim()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_ADD -> TagFilterAddViewHolder(parent)
            VIEW_TYPE_NORMAL -> TagFilterViewHolder(parent)
            VIEW_TYPE_ORDER -> TagFilterOrderViewHolder(parent)
            VIEW_TYPE_RATING -> TagFilterRatingViewHolder(parent)
            VIEW_TYPE_THRESHOLD -> TagFilterThresholdViewHolder(parent)
            else -> TagFilterSubheadViewHolder(parent)
        }

    override fun getItemViewType(position: Int): Int {
        val orderHeadPos = tags.size + 1
        val ratingHeadPos = orderHeadPos + orders.size + 1
        val thresholdHeadPos = ratingHeadPos + ratings.size + 1
        return when (position) {
            0 -> VIEW_TYPE_ADD
            in 1 until orderHeadPos -> VIEW_TYPE_NORMAL
            orderHeadPos -> VIEW_TYPE_ORDER_HEAD
            in (orderHeadPos + 1) until ratingHeadPos -> VIEW_TYPE_ORDER
            ratingHeadPos -> VIEW_TYPE_RATING_HEAD
            in (ratingHeadPos + 1) until thresholdHeadPos -> VIEW_TYPE_RATING
            thresholdHeadPos -> VIEW_TYPE_THRESHOLD_HEAD
            else -> VIEW_TYPE_THRESHOLD
        }
    }

    override fun getItemCount(): Int {
        var count = 0
        if (booruType == BOORU_TYPE_UNKNOWN) {
            return count
        }
        count += tags.size + orders.size + ratings.size + 3
        if (thresholds.isNotEmpty()) {
            count += thresholds.size + 1
        }
        return count
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TagFilterViewHolder -> holder.bindTo(tags[position - 1])
            is TagFilterSubheadViewHolder -> {
                when (position) {
                    tags.size + 1 -> holder.bind(holder.itemView.context.getString(R.string.order))
                    tags.size + orders.size + 2 -> holder.bind(holder.itemView.context.getString(R.string.rating))
                    else -> holder.bind(holder.itemView.context.getString(R.string.threshold))
                }
            }
            is TagFilterOrderViewHolder -> holder.bindTo(orders[position - tags.size - 2])
            is TagFilterRatingViewHolder -> holder.bindTo(ratings[position - tags.size - orders.size - 3])
            is TagFilterThresholdViewHolder -> holder.bindTo(thresholds[position - tags.size - orders.size - ratings.size - 4])
        }
    }

    inner class TagFilterAddViewHolder(parent: ViewGroup) : BaseTagFilterViewHolder(parent = parent) {
        init {
            tagFilterView.apply {
                text = itemView.context.getString(R.string.tag_filter_add_text)
                isCheckable = false
                chipIconTint = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorAccent))
                setOnClickListener {
                    addSearchBarTextCallback.invoke()
                }
            }
        }
    }

    inner class TagFilterOrderViewHolder(parent: ViewGroup) : BaseTagFilterViewHolder(parent = parent) {
        private var order: String = ""
        init {
            tagFilterView.apply {
                setOnClickListener {
                    orderSelected = if (isChecked) {
                        if (orderSelected.isNotBlank() && order != orderSelected) {
                            refreshOrder(orderSelected)
                        }
                        order
                    } else { "" }
                }
            }
        }
        fun bindTo(order: String) {
            this.order = order
            val checkedState = order == orderSelected
            tagFilterView.apply {
                text = order
                tag = order
                isChecked = checkedState
            }
        }
    }

    inner class TagFilterRatingViewHolder(parent: ViewGroup) : BaseTagFilterViewHolder(parent = parent) {
        private var rating: String = ""
        init {
            tagFilterView.apply {
                setOnCheckedChangeListener { _, isChecked ->
                    ratingSelected = if (isChecked) {
                        if (ratingSelected.isNotBlank() && rating != ratingSelected) {
                            refreshRating(ratingSelected)
                        }
                        rating
                    } else { "" }
                }
            }
        }
        fun bindTo(rating: String) {
            this.rating = rating
            val checkedState = rating == ratingSelected
            tagFilterView.apply {
                text = rating
                tag = rating
                isChecked = checkedState
            }
        }
    }

    inner class TagFilterThresholdViewHolder(parent: ViewGroup) : BaseTagFilterViewHolder(parent = parent) {
        private var threshold: String = ""
        init {
            tagFilterView.apply {
                setOnClickListener {
                    thresholdSelected = if (isChecked) {
                        if (thresholdSelected.isNotBlank() && threshold != thresholdSelected) {
                            refreshThreshold(thresholdSelected)
                        }
                        threshold
                    } else { "" }
                }
            }
        }
        fun bindTo(threshold: String) {
            this.threshold = threshold
            val checkedState = threshold == thresholdSelected
            tagFilterView.apply {
                text = threshold
                tag = threshold
                isChecked = checkedState
            }
        }
    }

    private fun deleteTag(tag: TagFilter?) {
        if (tag == null) {
            return
        }
        deleteTagCallback.invoke(tag)
    }

    inner class TagFilterViewHolder(parent: ViewGroup) : BaseTagFilterViewHolder(parent = parent) {
        private var tagFilter: TagFilter? = null
        init {
            tagFilterView.apply {
                setOnClickListener {
                    tagFilter?.let {
                        if (isChecked) {
                            tagsSelected.add(it)
                        } else {
                            tagsSelected.remove(it)
                        }
                    }
                }
                setOnLongClickListener {
                    deleteTag(tagFilter)
                    true
                }
            }
        }

        fun bindTo(tag: TagFilter) {
            tagFilter = tag
            val name = tag.name
            tagFilterView.apply {
                text = name
                isChecked = tagsSelected.contains(tag)
            }
        }
    }

    class TagFilterSubheadViewHolder(binding: ItemTagFilterSubheadBinding) : RecyclerView.ViewHolder(binding.root) {
        constructor(parent: ViewGroup): this(parent.viewBinding(ItemTagFilterSubheadBinding::inflate))
        private val subhead = binding.subhead
        fun bind(name: String) {
            subhead.text = name
        }
    }
}
