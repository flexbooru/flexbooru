package onlymash.flexbooru.ui.adapter

import android.app.Activity
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.model.PostMoe
import onlymash.flexbooru.repository.NetworkState
import onlymash.flexbooru.ui.viewholder.HeadViewHolder
import onlymash.flexbooru.ui.viewholder.NetworkStateViewHolder
import onlymash.flexbooru.ui.viewholder.PostMoeViewHolder

class PostMoeAdapter(private val glide: GlideRequests,
                     private val activity: Activity,
                     private val retryCallback: () -> Unit) : PagedListAdapter<PostMoe, RecyclerView.ViewHolder>(POST_COMPARATOR) {

    companion object {
        val POST_COMPARATOR = object : DiffUtil.ItemCallback<PostMoe>() {
            override fun areContentsTheSame(oldItem: PostMoe, newItem: PostMoe): Boolean =
                oldItem == newItem
            override fun areItemsTheSame(oldItem: PostMoe, newItem: PostMoe): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.head_item -> HeadViewHolder.create(parent)
            R.layout.post_item -> PostMoeViewHolder.create(parent, glide, activity)
            R.layout.network_state_item -> NetworkStateViewHolder.create(parent, retryCallback)
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.head_item -> { }
            R.layout.post_item -> {
                (holder as PostMoeViewHolder).bind(getItem(position - 1))
            }
            R.layout.network_state_item -> {
                (holder as NetworkStateViewHolder).bindTo(networkState)
            }
        }
    }

    private var networkState: NetworkState? = null

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            R.layout.head_item
        } else if (hasExtraRow() && position == itemCount - 1) {
            R.layout.network_state_item
        } else {
            R.layout.post_item
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