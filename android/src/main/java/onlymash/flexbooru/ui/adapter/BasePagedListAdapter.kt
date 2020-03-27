package onlymash.flexbooru.ui.adapter

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import onlymash.flexbooru.R
import onlymash.flexbooru.data.repository.NetworkState
import onlymash.flexbooru.ui.viewholder.NetworkStateViewHolder

abstract class BasePagedListAdapter<T, VH : RecyclerView.ViewHolder>(
    diffCallback: DiffUtil.ItemCallback<T>,
    private val retryCallback: () -> Unit
) : PagedListAdapter<T, VH>(diffCallback) {

    private var networkState: NetworkState? = null

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            R.layout.item_network_state
        } else {
            super.getItemViewType(position)
        }
    }

    abstract fun onCreateItemViewHolder(parent: ViewGroup, viewType: Int): VH

    abstract fun onBindItemViewHolder(holder: VH, position: Int)

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return when (viewType) {
            R.layout.item_network_state -> NetworkStateViewHolder.create(parent, retryCallback) as VH
            else -> onCreateItemViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        when (getItemViewType(position)) {
            R.layout.item_network_state -> {
                if (holder.itemView.layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                    (holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
                }
                (holder as NetworkStateViewHolder).bindTo(networkState)
            }
            else -> {
                onBindItemViewHolder(holder, position)
            }
        }
    }

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }
}