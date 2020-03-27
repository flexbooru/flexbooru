package onlymash.flexbooru.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.extension.toVisibility
import onlymash.flexbooru.glide.GlideRequests

private const val MAX_ASPECT_RATIO = 21.0 / 9.0
private const val MIN_ASPECT_RATIO = 9.0 / 21.0

class PostAdapter(
    private val glide: GlideRequests,
    retryCallback: () -> Unit
) : BasePagedListAdapter<Post, RecyclerView.ViewHolder>(POST_COMPARATOR, retryCallback) {

    companion object {
        val POST_COMPARATOR = object : DiffUtil.ItemCallback<Post>() {
            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem == newItem
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem.booruUid == newItem.booruUid &&
                        oldItem.query == newItem.query &&
                        oldItem.id == newItem.id
        }
    }

    private val showInfoBar = Settings.showInfoBar

    override fun onCreateItemViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        PostViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false))

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PostViewHolder).bindTo(getItem(position))
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val preview: AppCompatImageView = itemView.findViewById(R.id.preview)
        private val infoContainer: LinearLayout = itemView.findViewById(R.id.info_container)
        private val postId: AppCompatTextView = itemView.findViewById(R.id.post_id)
        private val postSize: AppCompatTextView = itemView.findViewById(R.id.post_size)

        private var post: Post? = null

        init {
            infoContainer.toVisibility(showInfoBar)
        }

        fun bindTo(post: Post?) {
            this.post = post ?: return
            postId.text = String.format("#%d", post.id)
            postSize.text = String.format("%d x %d", post.width, postSize.height)
            val placeholderDrawable = when (post.rating) {
                "s" -> itemView.resources.getDrawable(R.drawable.background_rating_s, itemView.context.theme)
                "q" -> itemView.resources.getDrawable(R.drawable.background_rating_q, itemView.context.theme)
                else -> itemView.resources.getDrawable(R.drawable.background_rating_e, itemView.context.theme)
            }
            val ratio = post.width.toFloat() / post.height.toFloat()
            (preview.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio =
                when {
                    ratio > MAX_ASPECT_RATIO -> "H, 21:9"
                    ratio < MIN_ASPECT_RATIO -> "H, 9:21"
                    else -> "H, ${post.width}:${post.height}"
                }
            glide.load(post.preview)
                .placeholder(placeholderDrawable)
                .into(preview)
        }
    }
}