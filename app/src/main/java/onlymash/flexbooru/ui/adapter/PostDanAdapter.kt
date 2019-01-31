package onlymash.flexbooru.ui.adapter

import android.app.Activity
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.model.PostDan
import onlymash.flexbooru.ui.viewholder.PostDanViewHolder

class PostDanAdapter(private val glide: GlideRequests,
                     private val activity: Activity): PagedListAdapter<PostDan, RecyclerView.ViewHolder>(POST_COMPARATOR) {

    companion object {
        val POST_COMPARATOR = object : DiffUtil.ItemCallback<PostDan>() {
            override fun areContentsTheSame(oldItem: PostDan, newItem: PostDan): Boolean =
                oldItem == newItem
            override fun areItemsTheSame(oldItem: PostDan, newItem: PostDan): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        PostDanViewHolder.create(parent, glide, activity)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PostDanViewHolder).bind(getItem(position))
    }
}