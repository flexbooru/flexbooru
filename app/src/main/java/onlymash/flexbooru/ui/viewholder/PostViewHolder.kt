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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.entity.post.*
import onlymash.flexbooru.glide.GlideRequests

class PostViewHolder(itemView: View,
                     showInfoBar: Boolean,
                     private val glide: GlideRequests): RecyclerView.ViewHolder(itemView){

    companion object {
        fun create(parent: ViewGroup, showInfoBar: Boolean, glide: GlideRequests): PostViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_post, parent, false)
            return PostViewHolder(view, showInfoBar, glide)
        }
    }

    private val previewCard: CardView = itemView.findViewById(R.id.preview_card)
    private val preview: ImageView = itemView.findViewById(R.id.preview)
    private val infoContainer: LinearLayout = itemView.findViewById(R.id.info_container)
    private val postId: TextView = itemView.findViewById(R.id.post_id)
    private val postSize: TextView = itemView.findViewById(R.id.post_size)

    private var post: BasePost? = null

    private var itemListener: ItemListener? = null

    init {
        if (showInfoBar) {
            infoContainer.visibility = View.VISIBLE
        }
        itemView.setOnClickListener {
            itemListener?.onClickItem(post, preview)
        }
        itemView.setOnLongClickListener {
            itemListener?.onLongClickItem(post)
            true
        }
    }

    fun bind(post: BasePost?, pageType: Int) {
        if (post == null) return
        this.post = post
        previewCard.tag = post.getPostId()
        preview.transitionName = when (pageType) {
            Constants.PAGE_TYPE_POST -> preview.context.getString(R.string.post_transition_name, post.getPostId())
            else -> preview.context.getString(R.string.post_popular_transition_name, post.getPostId())
        }
        postId.text = String.format("#%d", post.getPostId())
        postSize.text = String.format("%d x %d", post.getPostWidth(), post.getPostHeight())
        val placeholderDrawable = when (post.getPostRating()) {
            "s" -> itemView.resources.getDrawable(R.drawable.background_rating_s, itemView.context.theme)
            "q" -> itemView.resources.getDrawable(R.drawable.background_rating_q, itemView.context.theme)
            else -> itemView.resources.getDrawable(R.drawable.background_rating_e, itemView.context.theme)
        }
        val lp = preview.layoutParams as ConstraintLayout.LayoutParams
        val ratio = post.getPostWidth().toFloat()/post.getPostHeight().toFloat()
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
        preview.layoutParams = lp
        preview.layout(0,0,0,0)
        glide.load(post.getPreviewUrl())
            .placeholder(placeholderDrawable)
            .into(preview)
    }

    fun setItemListener(listener: ItemListener) {
        itemListener = listener
    }

    interface ItemListener {
        fun onClickItem(post: BasePost?, view: View)
        fun onLongClickItem(post: BasePost?)
    }
}