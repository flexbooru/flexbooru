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
import onlymash.flexbooru.model.PostDan

class PostDanViewHolder(itemView: View,
       private val glide: GlideRequests,
       private val activity: Activity): RecyclerView.ViewHolder(itemView){

    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests, activity: Activity): PostDanViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_post, parent, false)
            return PostDanViewHolder(view, glide, activity)
        }
    }

    private val preview: ImageView = itemView.findViewById(R.id.preview)
    private var postDan: PostDan? = null

    fun bind(post: PostDan?) {
        postDan = post
        if (post is PostDan && !post.preview_file_url.isNullOrEmpty()) {
            val lp = preview.layoutParams
            if (lp is FlexboxLayoutManager.LayoutParams) {
                lp.flexGrow = 1f
            }
            val placeholder = when (post.rating) {
                "s" -> R.drawable.background_rating_s
                "q" -> R.drawable.background_rating_q
                else -> R.drawable.background_rating_e
            }
            if (post.image_width < post.image_height) {
                lp.height = activity.resources.getDimensionPixelSize(R.dimen.post_item_height_max)
                lp.width = lp.height * post.image_width/post.image_height
            } else {
                lp.height = activity.resources.getDimensionPixelSize(R.dimen.post_item_height_min)
                lp.width = lp.height * post.image_width/post.image_height
            }
            glide.load(FlexGlideUrl(post.preview_file_url))
                .placeholder(activity.resources.getDrawable(placeholder, activity.theme))
                .centerCrop()
                .into(preview)
        }
    }
}