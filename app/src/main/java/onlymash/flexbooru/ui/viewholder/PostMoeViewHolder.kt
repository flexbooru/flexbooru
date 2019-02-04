package onlymash.flexbooru.ui.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.model.Placeholder
import onlymash.flexbooru.model.PostMoe

class PostMoeViewHolder(itemView: View,
                        private val glide: GlideRequests,
                        private val placeholder: Placeholder): RecyclerView.ViewHolder(itemView){

    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests, placeholder: Placeholder): PostMoeViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_post, parent, false)
            return PostMoeViewHolder(view, glide, placeholder)
        }
    }

    private val preview: ImageView = itemView.findViewById(R.id.preview)
    private val previewCard: CardView = itemView.findViewById(R.id.preview_card)
    private var postMoe: PostMoe? = null

    fun bind(post: PostMoe?) {
        postMoe = post
        if (post is PostMoe) {
            val placeholderDrawable = when (post.rating) {
                "s" -> placeholder.s
                "q" -> placeholder.q
                else -> placeholder.e
            }
            val lp = previewCard.layoutParams as ConstraintLayout.LayoutParams
            val ratio = post.width.toFloat()/post.height.toFloat()
            when {
                ratio > Constants.MAX_ITEM_ASPECT_RATIO -> {
                    lp.dimensionRatio = "H, ${Constants.MAX_ITEM_ASPECT_RATIO}:1"
                }
                ratio < Constants.MIN_ITEM_ASPECT_RATIO -> {
                    lp.dimensionRatio = "H, ${Constants.MIN_ITEM_ASPECT_RATIO}:1"
                }
                else -> {
                    lp.dimensionRatio = "H, $ratio:1"
                }
            }
            previewCard.layoutParams = lp
            glide.load(post.preview_url)
                .placeholder(placeholderDrawable)
                .centerCrop()
                .into(preview)
        }
    }
}