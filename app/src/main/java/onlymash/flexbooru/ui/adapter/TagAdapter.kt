package onlymash.flexbooru.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.entity.TagDan
import onlymash.flexbooru.entity.TagMoe
import onlymash.flexbooru.ui.viewholder.TagViewHolder

class TagAdapter(private val listener: TagViewHolder.ItemListener,
                 retryCallback: () -> Unit) : BaseStatePagedListAdapter<Any, RecyclerView.ViewHolder>(TAG_COMPARATOR, retryCallback) {

    companion object {
        val TAG_COMPARATOR = object : DiffUtil.ItemCallback<Any>() {
            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean = oldItem == newItem
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                return when {
                    oldItem is TagDan && newItem is TagDan -> oldItem.id == newItem.id
                    oldItem is TagMoe && newItem is TagMoe -> oldItem.id == newItem.id
                    else -> false
                }
            }
        }
    }

    override fun onCreateDataViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        TagViewHolder.create(parent)

    override fun onBindDataViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TagViewHolder) {
            holder.apply {
                setItemListener(listener)
                bind(getItem(position))
            }
        }
    }
}