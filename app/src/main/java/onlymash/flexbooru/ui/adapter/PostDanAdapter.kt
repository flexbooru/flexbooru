package onlymash.flexbooru.ui.adapter

import android.app.Activity
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.model.PostDan
import onlymash.flexbooru.repository.NetworkState
import onlymash.flexbooru.ui.viewholder.HeadViewHolder
import onlymash.flexbooru.ui.viewholder.NetworkStateViewHolder
import onlymash.flexbooru.ui.viewholder.PostDanViewHolder

class PostDanAdapter(private val glide: GlideRequests,
                     private val activity: Activity,
                     private val retryCallback: () -> Unit) : PagedListAdapter<PostDan, RecyclerView.ViewHolder>(POST_COMPARATOR) {

    companion object {
        val POST_COMPARATOR = object : DiffUtil.ItemCallback<PostDan>() {
            override fun areContentsTheSame(oldItem: PostDan, newItem: PostDan): Boolean =
                oldItem == newItem
            override fun areItemsTheSame(oldItem: PostDan, newItem: PostDan): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_head -> HeadViewHolder.create(parent)
            R.layout.item_post -> PostDanViewHolder.create(parent, glide, activity)
            R.layout.item_network_state -> NetworkStateViewHolder.create(parent, retryCallback)
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.item_head -> { }
            R.layout.item_post -> {
                (holder as PostDanViewHolder).bind(getItem(position - 1))
            }
            R.layout.item_network_state -> {
                (holder as NetworkStateViewHolder).bindTo(networkState)
            }
        }
    }

    private var networkState: NetworkState? = null

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            R.layout.item_head
        } else if (hasExtraRow() && position == itemCount - 1) {
            R.layout.item_network_state
        } else {
            R.layout.item_post
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 2 else 1
    }

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount() + 1)
            } else {
                notifyItemInserted(super.getItemCount() + 1)
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }
}