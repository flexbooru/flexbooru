package onlymash.flexbooru.ui.viewholder

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import onlymash.flexbooru.R
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.model.PostMoe

class PostMoeViewHolder(itemView: View,
                        private val glide: GlideRequests,
                        private val activity: Activity): RecyclerView.ViewHolder(itemView){

    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests, activity: Activity): PostMoeViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_post, parent, false)
            return PostMoeViewHolder(view, glide, activity)
        }
    }

    private val preview: ImageView = itemView.findViewById(R.id.preview)
    private var postMoe: PostMoe? = null

    fun bind(post: PostMoe?) {
        postMoe = post
        if (post is PostMoe) {
            val placeholder = when (post.rating) {
                "s" -> R.drawable.background_rating_s
                "q" -> R.drawable.background_rating_q
                else -> R.drawable.background_rating_e
            }
            val lp = preview.layoutParams
            if (lp is FlexboxLayoutManager.LayoutParams) {
                val ratio = post.width.toFloat()/post.height.toFloat()
                lp.flexGrow = 1f
                if (post.width < post.height) {
                    lp.height = activity.resources.getDimensionPixelSize(R.dimen.post_item_height_max)
                } else {
                    lp.height = activity.resources.getDimensionPixelSize(R.dimen.post_item_height_min)
                }
                when {
                    ratio > 0.7f -> lp.width = (lp.height * 0.7f).toInt()
                    ratio < 0.5f -> lp.width = (lp.height * 0.5f).toInt()
                    else -> lp.width = (lp.height * post.width.toFloat()/post.height.toFloat()).toInt()
                }
            }

            glide.load(post.preview_url)
                .placeholder(activity.resources.getDrawable(placeholder, activity.theme))
                .centerCrop()
                .into(preview)
        }
    }
}