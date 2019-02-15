/*
 * Copyright (C) 2019 by onlymash <im@fiepi.me>, All rights reserved
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package onlymash.flexbooru.ui.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.github.chrisbanes.photoview.PhotoView
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.entity.PostDan
import onlymash.flexbooru.entity.PostMoe

class PostViewHolder(itemView: View, private val glide: GlideRequests): RecyclerView.ViewHolder(itemView){

    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests): PostViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_post, parent, false)
            return PostViewHolder(view, glide)
        }
    }

    private val preview: PhotoView = itemView.findViewById(R.id.preview)
    private val previewCard: CardView = itemView.findViewById(R.id.preview_card)
    private var post: Any? = null

    private var itemListener: ItemListener? = null

    init {
        preview.isEnabled = false
        itemView.setOnClickListener {
            when (post) {
                is PostDan -> itemListener?.onClickDanItem(post as PostDan, preview)
                is PostMoe -> itemListener?.onClickMoeItem(post as PostMoe, preview)
            }
        }
    }

    fun bind(post: Any?) {
        when (post) {
            is PostDan -> {
                this.post = post
                preview.transitionName = String.format(preview.context.getString(R.string.post_transition_name), post.id)
                previewCard.tag = post.id
                val placeholderDrawable = when (post.rating) {
                    "s" -> itemView.resources.getDrawable(R.drawable.background_rating_s, itemView.context.theme)
                    "q" -> itemView.resources.getDrawable(R.drawable.background_rating_q, itemView.context.theme)
                    else -> itemView.resources.getDrawable(R.drawable.background_rating_e, itemView.context.theme)
                }
                val lp = previewCard.layoutParams as ConstraintLayout.LayoutParams
                val ratio = post.image_width.toFloat()/post.image_height.toFloat()
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
                glide.load(post.preview_file_url)
                    .placeholder(placeholderDrawable)
                    .centerCrop()
                    .into(preview)

            }
            is PostMoe -> {
                this.post = post
                preview.transitionName = String.format(preview.context.getString(R.string.post_transition_name), post.id)
                previewCard.tag = post.id
                val placeholderDrawable = when (post.rating) {
                    "s" -> itemView.resources.getDrawable(R.drawable.background_rating_s, itemView.context.theme)
                    "q" -> itemView.resources.getDrawable(R.drawable.background_rating_q, itemView.context.theme)
                    else -> itemView.resources.getDrawable(R.drawable.background_rating_e, itemView.context.theme)
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

    fun setItemListener(listener: ItemListener) {
        itemListener = listener
    }

    interface ItemListener {
        fun onClickDanItem(post: PostDan, view: View)
        fun onClickMoeItem(post: PostMoe, view: View)
    }
}