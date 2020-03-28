package onlymash.flexbooru.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.data.database.TagFilterManager
import onlymash.flexbooru.data.model.common.TagFilter
import onlymash.flexbooru.widget.TagFilterView

class TagFilterAdapter(private val orders: Array<String>,
                       private val ratings: Array<String>,
                       private val thresholds: Array<String>,
                       private val booruType: Int,
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

    private var orderSelected = ""
    private var ratingSelected = ""
    private var thresholdSelected = ""
    private val tagsSelected: MutableList<String> = mutableListOf()

    private var refreshingOrder = false
    private var refreshingRating = false
    private var refreshingThreshold = false

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

    private fun refreshOrder(order: String) {
        val index = orders.indexOfFirst { it == order }
        refreshingOrder = true
        notifyItemChanged(tags.size + index + 2)
    }

    private fun refreshRating(rating: String) {
        val index = ratings.indexOfFirst { it == rating }
        refreshingRating = true
        notifyItemChanged(tags.size + orders.size + index + 3)
    }

    private fun refreshThreshold(threshold: String) {
        val index = thresholds.indexOfFirst { it == threshold }
        refreshingThreshold = true
        notifyItemChanged(tags.size + orders.size + ratings.size + index + 4)
    }

    fun updateData(newTags: List<TagFilter>, booruUid: Long, showAll: Boolean) {
        allTags.clear()
        allTags.addAll(newTags)
        val oldTags: MutableList<TagFilter> = mutableListOf()
        oldTags.addAll(tags)
        tags.clear()
        if (showAll) {
            tags.addAll(newTags)
        } else {
            newTags.forEach {
                if (it.booruUid == booruUid) {
                    tags.add(it)
                }
            }
        }
        val diffResult = DiffUtil.calculateDiff(tagFilterDiffCallback(oldTags, tags))
        diffResult.dispatchUpdatesTo(listUpdateCallback)
    }
    fun updateData(booruUid: Long, showAll: Boolean) {
        tagsSelected.clear()
        val oldTags: MutableList<TagFilter> = mutableListOf()
        oldTags.addAll(tags)
        tags.clear()
        if (showAll) {
            tags.addAll(allTags)
        } else {
            allTags.forEach {
                if (it.booruUid == booruUid) {
                    tags.add(it)
                }
            }
        }
        val diffResult = DiffUtil.calculateDiff(tagFilterDiffCallback(oldTags, tags))
        diffResult.dispatchUpdatesTo(listUpdateCallback)
    }

    fun getSelectedTagsString(): String {
        var str = ""
        tagsSelected.forEach {
            str = String.format("%s %s", it, str)
        }
        str = str.trim()
        if (orderSelected.isNotEmpty()) {
            str = if (booruType == BOORU_TYPE_GEL) {
                "$str sort:$orderSelected"
            } else {
                "$str order:$orderSelected"
            }
        }
        if (ratingSelected.isNotEmpty()) {
            str = "$str rating:$ratingSelected"
        }
        if (thresholdSelected.isNotEmpty()) {
            str = "$str threshold:$thresholdSelected"
        }
        return str.trim()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_ADD -> TagFilterAddViewHolder.create(parent)
            VIEW_TYPE_NORMAL -> TagFilterViewHolder.create(parent)
            VIEW_TYPE_ORDER -> TagFilterOrderViewHolder.create(parent)
            VIEW_TYPE_RATING -> TagFilterRatingViewHolder.create(parent)
            VIEW_TYPE_THRESHOLD -> TagFilterThresholdViewHolder.create(parent)
            else -> TagFilterSubheadViewHolder.create(parent)
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
        var count = tags.size + orders.size + ratings.size + 3
        if (thresholds.isNotEmpty()) {
            count += thresholds.size + 1
        }
        return count
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TagFilterAddViewHolder -> {
                holder.itemView.setOnClickListener {
                    addSearchBarTextCallback()
                }
            }
            is TagFilterViewHolder -> {
                val tag = tags[position - 1]
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
                            .setTitle(context.getString(R.string.tag_delete_title))
                            .setMessage(String.format(context.getString(R.string.tag_delete_content), name))
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
                when (position) {
                    tags.size + 1 -> holder.bind(holder.itemView.context.getString(R.string.order))
                    tags.size + orders.size + 2 -> holder.bind(holder.itemView.context.getString(R.string.rating))
                    else -> holder.bind(holder.itemView.context.getString(R.string.threshold))
                }
            }
            is TagFilterOrderViewHolder -> {
                val order = orders[position - tags.size - 2]
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
                val rating = ratings[position - tags.size - orders.size - 3]
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
            is TagFilterThresholdViewHolder -> {
                val threshold = thresholds[position - tags.size - orders.size - ratings.size - 4]
                val checkedState = threshold == thresholdSelected
                (holder.itemView as TagFilterView).apply {
                    text = threshold
                    tag = threshold
                    if (refreshingThreshold) {
                        refreshingThreshold = false
                        animateCheckedAndInvoke(checkedState) {}
                    } else {
                        isChecked = checkedState
                    }
                    setOnClickListener {
                        val checked = !isChecked
                        animateCheckedAndInvoke(checked) {}
                        val oldSelected = thresholdSelected
                        thresholdSelected = if (checked) threshold else ""
                        if (oldSelected.isNotEmpty()) refreshThreshold(oldSelected)
                    }
                }
            }
        }
    }

    class TagFilterAddViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup): TagFilterAddViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_tag_filter, parent, false)
                return TagFilterAddViewHolder(view)
            }
        }
        init {
            (itemView as TagFilterView).apply {
                text = itemView.context.getString(R.string.tag_filter_add_text)
                color = ContextCompat.getColor(context, R.color.colorAccent)
                selectedTextColor = ContextCompat.getColor(context, R.color.white)
            }
        }
    }

    class TagFilterOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup): TagFilterOrderViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_tag_filter, parent, false)
                return TagFilterOrderViewHolder(view)
            }
        }
        init {
            (itemView as TagFilterView).apply {
                color = ContextCompat.getColor(itemView.context, R.color.colorPrimary)
                selectedTextColor = ContextCompat.getColor(itemView.context, R.color.white)
            }
        }
    }

    class TagFilterRatingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup): TagFilterRatingViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_tag_filter, parent, false)
                return TagFilterRatingViewHolder(view)
            }
        }
        init {
            (itemView as TagFilterView).apply {
                color = ContextCompat.getColor(itemView.context, R.color.colorPrimary)
                selectedTextColor = ContextCompat.getColor(itemView.context, R.color.white)
            }
        }
    }

    class TagFilterSubheadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup): TagFilterSubheadViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_tag_filter_subhead, parent, false)
                return TagFilterSubheadViewHolder(view)
            }
        }
        private val subhead = itemView.findViewById<AppCompatTextView>(R.id.subhead)
        fun bind(name: String) {
            subhead.text = name
        }
    }

    class TagFilterThresholdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup): TagFilterThresholdViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_tag_filter, parent, false)
                return TagFilterThresholdViewHolder(view)
            }
        }
        init {
            (itemView as TagFilterView).apply {
                color = ContextCompat.getColor(itemView.context, R.color.colorPrimary)
                selectedTextColor = ContextCompat.getColor(itemView.context, R.color.white)
            }
        }
    }

    class TagFilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup): TagFilterViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_tag_filter, parent, false)
                return TagFilterViewHolder(view)
            }
        }
        init {
            (itemView as TagFilterView).apply {
                color = ContextCompat.getColor(context, R.color.colorPrimary)
                selectedTextColor = ContextCompat.getColor(context, R.color.white)
            }
        }
    }
}