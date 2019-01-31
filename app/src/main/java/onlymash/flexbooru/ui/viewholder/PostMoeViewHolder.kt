package onlymash.flexbooru.ui.viewholder

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import onlymash.flexbooru.R
import onlymash.flexbooru.glide.FlexGlideUrl
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.model.PostMoe

class PostMoeViewHolder(itemView: View,
                        private val glide: GlideRequests,
                        private val activity: Activity): RecyclerView.ViewHolder(itemView){

    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests, activity: Activity): PostMoeViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.post_item, parent, false)
            return PostMoeViewHolder(view, glide, activity)
        }
    }

    private val preview: ImageView = itemView.findViewById(R.id.preview)
    private var postMoe: PostMoe? = null

    fun bind(post: PostMoe?) {
        postMoe = post
        if (post is PostMoe) {
            val lp = preview.layoutParams
            if (lp is FlexboxLayoutManager.LayoutParams) {
                lp.flexGrow = 1f
            }
            val placeholder = when (post.rating) {
                "s" -> R.drawable.background_rating_s
                "q" -> R.drawable.background_rating_q
                else -> R.drawable.background_rating_e
            }
            lp.width = lp.height * post.width/post.height
            glide.load(FlexGlideUrl(post.preview_url))
                .placeholder(activity.resources.getDrawable(placeholder, activity.theme))
                .centerCrop()
                .into(preview)
        }
    }
}