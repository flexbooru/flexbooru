package onlymash.flexbooru.ui.adapter

import android.view.ViewGroup
import androidx.paging.AsyncPagedListDiffer
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.*
import onlymash.flexbooru.R
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.model.Placeholder
import onlymash.flexbooru.model.PostDan
import onlymash.flexbooru.model.PostMoe
import onlymash.flexbooru.repository.NetworkState
import onlymash.flexbooru.ui.viewholder.HeaderViewHolder
import onlymash.flexbooru.ui.viewholder.NetworkStateViewHolder
import onlymash.flexbooru.ui.viewholder.PostViewHolder

class PostAdapter(private val glide: GlideRequests,
                  private val placeholder: Placeholder,
                  private val listener: PostViewHolder.ItemListener,
                  private val retryCallback: () -> Unit) : PagedListAdapter<Any, RecyclerView.ViewHolder>(POST_COMPARATOR) {

    companion object {
        val POST_COMPARATOR = object : DiffUtil.ItemCallback<Any>() {
            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean = oldItem == newItem
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                return when {
                    oldItem is PostDan && newItem is PostDan -> oldItem.id == newItem.id
                    oldItem is PostMoe && newItem is PostMoe -> oldItem.id == newItem.id
                    else -> false
                }
            }
        }
    }

    private val adapterCallback = AdapterListUpdateCallback(this)

    private val listUpdateCallback = object : ListUpdateCallback {
        override fun onChanged(position: Int, count: Int, payload: Any?) {
            adapterCallback.onChanged(position + 1, count, payload)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            adapterCallback.onMoved(fromPosition + 1, toPosition + 1)
        }

        override fun onInserted(position: Int, count: Int) {
            adapterCallback.onInserted(position + 1, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            adapterCallback.onRemoved(position + 1, count)
        }
    }

    private val differ = AsyncPagedListDiffer<Any>(listUpdateCallback,
        AsyncDifferConfig.Builder<Any>(POST_COMPARATOR).build())

    override fun getItem(position: Int): Any? {
        return differ.getItem(position - 1)
    }

    override fun submitList(pagedList: PagedList<Any>?) {
        differ.submitList(pagedList)
    }

    override fun getCurrentList(): PagedList<Any>? {
        return differ.currentList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_header -> HeaderViewHolder.create(parent)
            R.layout.item_post -> PostViewHolder.create(parent, glide, placeholder)
            R.layout.item_network_state -> NetworkStateViewHolder.create(parent, retryCallback)
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.item_header -> {
                if (holder.itemView.layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                    (holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
                }
            }
            R.layout.item_post -> {
                (holder as PostViewHolder).bind(getItem(position))
                holder.setItemListener(listener)
            }
            R.layout.item_network_state -> {
                if (holder.itemView.layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                    (holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
                }
                (holder as NetworkStateViewHolder).bindTo(networkState)
            }
        }
    }

    private var networkState: NetworkState? = null

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            R.layout.item_header
        } else if (hasExtraRow() && position == itemCount - 1) {
            R.layout.item_network_state
        } else {
            R.layout.item_post
        }
    }

    override fun getItemCount(): Int {
        return differ.itemCount + if (hasExtraRow()) 2 else 1
    }

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(differ.itemCount + 1)
            } else {
                notifyItemInserted(differ.itemCount + 1)
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }
}