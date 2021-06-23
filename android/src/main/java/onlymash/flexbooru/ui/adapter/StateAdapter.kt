package onlymash.flexbooru.ui.adapter

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import onlymash.flexbooru.ui.base.NetworkStateViewHolder

class StateAdapter<T : Any, VH : RecyclerView.ViewHolder>(
    private val adapter: PagingDataAdapter<T, VH>,
) : LoadStateAdapter<NetworkStateViewHolder>() {

    override fun onBindViewHolder(holder: NetworkStateViewHolder, loadState: LoadState) {
        val layoutParams = holder.itemView.layoutParams
        if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
            layoutParams.isFullSpan = true
        }
        holder.bindTo(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): NetworkStateViewHolder {
        return NetworkStateViewHolder(parent) {
            adapter.retry()
        }
    }
}