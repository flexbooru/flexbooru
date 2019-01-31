package onlymash.flexbooru.ui.adapter

import android.app.Activity
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.model.PostMoe
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        PostMoeViewHolder.create(parent, glide, activity)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PostMoeViewHolder).bind(getItem(position))
    }
}