package onlymash.flexbooru.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.entity.PoolDan
import onlymash.flexbooru.entity.PoolMoe
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.ui.viewholder.PoolViewHolder

class PoolAdapter(private val glide: GlideRequests,
                  private val listener: PoolViewHolder.ItemListener,
                  retryCallback: () -> Unit
) : BaseStatePagedListAdapter<Any, RecyclerView.ViewHolder>(POOL_COMPARATOR, retryCallback) {

    companion object {
        val POOL_COMPARATOR = object : DiffUtil.ItemCallback<Any>() {
            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean = oldItem == newItem
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                return when {
                    oldItem is PoolDan && newItem is PoolDan -> oldItem.id == newItem.id
                    oldItem is PoolMoe && newItem is PoolMoe -> oldItem.id == newItem.id
                    else -> false
                }
            }
        }
    }

    override fun onCreateDataViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        PoolViewHolder.create(parent, glide)

    override fun onBindDataViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PoolViewHolder) {
            holder.apply {
                setItemListener(listener)
                bind(getItem(position))
            }
        }
    }
}