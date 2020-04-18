/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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

package onlymash.flexbooru.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings.GRID_MODE_FIXED
import onlymash.flexbooru.common.Settings.gridMode
import onlymash.flexbooru.common.Settings.gridRatio
import onlymash.flexbooru.common.Settings.isLargeWidth
import onlymash.flexbooru.common.Settings.showInfoBar
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.extension.isStillImage
import onlymash.flexbooru.glide.GlideRequests

private const val MAX_ASPECT_RATIO = 21.0 / 9.0
private const val MIN_ASPECT_RATIO = 9.0 / 21.0

class PostAdapter(
    private val glide: GlideRequests,
    private val clickItemCallback: (View, Int, String) -> Unit,
    private val longClickItemCallback: (Post) -> Unit,
    retryCallback: () -> Unit
) : BasePagedListAdapter<Post, RecyclerView.ViewHolder>(POST_COMPARATOR, retryCallback) {

    companion object {
        val POST_COMPARATOR = object : DiffUtil.ItemCallback<Post>() {
            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem.id == newItem.id
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem.booruUid == newItem.booruUid &&
                        oldItem.query == newItem.query &&
                        oldItem.id == newItem.id
        }
    }

    var isShowBar = showInfoBar
    var isLargeItemWidth = isLargeWidth
    var isRatioFixed = gridMode == GRID_MODE_FIXED
    var itemRatio = gridRatio

    override fun onCreateItemViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        PostViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false))

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val post = try {
            getItem(position)
        } catch (_: IndexOutOfBoundsException) {
            null
        }
        (holder as PostViewHolder).bindTo(post)
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val preview: AppCompatImageView = itemView.findViewById(R.id.preview)
        private val infoContainer: LinearLayout = itemView.findViewById(R.id.info_container)
        private val postId: AppCompatTextView = itemView.findViewById(R.id.post_id)
        private val postSize: AppCompatTextView = itemView.findViewById(R.id.post_size)

        private var post: Post? = null

        init {
            infoContainer.isVisible = isShowBar
            itemView.setOnClickListener {
                post?.let {
                    clickItemCallback(preview, layoutPosition, "post_${it.id}")
                }
            }
            itemView.setOnLongClickListener {
                post?.let {
                    longClickItemCallback(it)
                }
                true
            }
        }

        private fun getPlaceholderDrawable(@DrawableRes drawableRes: Int) =
            ResourcesCompat.getDrawable(itemView.context.resources, drawableRes, itemView.context.theme)

        fun bindTo(post: Post?) {
            this.post = post ?: return
            infoContainer.isVisible = isShowBar
            postId.text = String.format("#%d", post.id)
            postSize.text = String.format("%d x %d", post.width, post.height)
            val placeholderDrawable = getPlaceholderDrawable(
                when (post.rating) {
                    "s" -> R.drawable.background_rating_s
                    "q" -> R.drawable.background_rating_q
                    else -> R.drawable.background_rating_e
                }
            )
            (preview.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio =
                if (isRatioFixed) {
                    preview.scaleType = ImageView.ScaleType.CENTER_CROP
                    "H, $itemRatio"
                } else {
                    preview.scaleType = ImageView.ScaleType.FIT_CENTER
                    val ratio = post.width.toFloat() / post.height.toFloat()
                    when {
                        ratio > MAX_ASPECT_RATIO -> "H, 21:9"
                        ratio < MIN_ASPECT_RATIO -> "H, 9:21"
                        else -> "H, ${post.width}:${post.height}"
                    }
                }
            preview.transitionName = "post_${post.id}"
            val url = if(isLargeItemWidth && post.sample.isStillImage())
                post.sample
            else
                post.preview
            glide.load(url)
                .placeholder(placeholderDrawable)
                .transition(withCrossFade())
                .into(preview)
        }
    }
}