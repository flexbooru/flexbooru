package onlymash.flexbooru.ui.adapter

import android.app.Activity
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.model.PostMoe
import onlymash.flexbooru.ui.viewholder.HeadViewHolder
import onlymash.flexbooru.ui.viewholder.PostMoeViewHolder

class PostMoeAdapter(private val glide: GlideRequests,
                     private val activity: Activity): PagedListAdapter<PostMoe, RecyclerView.ViewHolder>(POST_COMPARATOR) {

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
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            R.layout.head_item
        } else {
            R.layout.post_item
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position > 0) (holder as PostMoeViewHolder).bind(getItem(position - 1))
    }
}