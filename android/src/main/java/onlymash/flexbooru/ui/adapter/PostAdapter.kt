/*
 * Copyright (C) 2020. by onlymash <fiepi.dev@gmail.com>, All rights reserved
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

import android.annotation.SuppressLint
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
import androidx.core.view.updateLayoutParams
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import coil.load
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Settings.gridMode
import onlymash.flexbooru.app.Settings.gridRatio
import onlymash.flexbooru.app.Settings.isLargeWidth
import onlymash.flexbooru.app.Settings.isRoundedGrid
import onlymash.flexbooru.app.Settings.showInfoBar
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.databinding.ItemPostBinding
import onlymash.flexbooru.databinding.ItemPostRoundedBinding
import onlymash.flexbooru.extension.isStillImage
import onlymash.flexbooru.ui.viewbinding.viewBinding

private const val MAX_ASPECT_RATIO = 21.0 / 9.0
private const val MIN_ASPECT_RATIO = 9.0 / 21.0

@SuppressLint("NotifyDataSetChanged")
class PostAdapter(
    private val clickItemCallback: (View, Int, String) -> Unit,
    private val longClickItemCallback: (Post) -> Unit
) : PagingDataAdapter<Post, PostAdapter.PostViewHolder>(POST_COMPARATOR) {

    companion object {
        val POST_COMPARATOR = object : DiffUtil.ItemCallback<Post>() {
            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem.id == newItem.id
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem.booruUid == newItem.booruUid && oldItem.query == newItem.query && oldItem.id == newItem.id
        }
    }

    var isLargeItemWidth = isLargeWidth
    var isShowBar = showInfoBar
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var isRatioFixed = gridMode == "fixed"
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var itemRatio = gridRatio
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var isRounded = isRoundedGrid
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        return PostViewHolder(parent)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    fun withLoadStateFooterSafe(
        footer: LoadStateAdapter<*>
    ): ConcatAdapter {
        val containerAdapter = ConcatAdapter(this)
        addLoadStateListener { loadStates ->
            footer.loadState = loadStates.append
            if (loadStates.append is LoadState.Error && !containerAdapter.adapters.contains(footer)) {
                containerAdapter.addAdapter(footer)
                footer.loadState = loadStates.append
            } else if (containerAdapter.adapters.contains(footer)){
                containerAdapter.removeAdapter(footer)
            }
        }
        return containerAdapter
    }

    inner class PostViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

        constructor(parent: ViewGroup): this(
            if (isRounded) {
                parent.viewBinding(ItemPostRoundedBinding::inflate)
            } else {
                parent.viewBinding(ItemPostBinding::inflate)
            }
        )

        private val preview: AppCompatImageView
        private val infoContainer: LinearLayout
        private val postId: AppCompatTextView
        private val postSize: AppCompatTextView

        private var post: Post? = null

        init {
            val itemBinding = if (binding is ItemPostBinding) {
                binding
            } else {
                (binding as ItemPostRoundedBinding).layoutItemPost
            }
            preview = itemBinding.preview
            infoContainer = itemBinding.infoContainer
            postId = itemBinding.postId
            postSize = itemBinding.postSize

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
                    "g" -> R.drawable.background_rating_g
                    else -> R.drawable.background_rating_e
                }
            )
            val ratio = if (isRatioFixed) {
                preview.scaleType = ImageView.ScaleType.CENTER_CROP
                itemRatio
            } else {
                preview.scaleType = ImageView.ScaleType.FIT_CENTER
                val ratio = post.width.toFloat() / post.height.toFloat()
                when {
                    ratio > MAX_ASPECT_RATIO -> "21:9"
                    ratio < MIN_ASPECT_RATIO -> "9:21"
                    else -> "${post.width}:${post.height}"
                }
            }
            preview.updateLayoutParams<ConstraintLayout.LayoutParams> {
                dimensionRatio = ratio
            }
            preview.transitionName = "post_${post.id}"
            val url = if(isLargeItemWidth && post.sample.isStillImage())
                post.sample
            else
                post.preview
            preview.load(url) {
                placeholder(placeholderDrawable)
            }
        }
    }
}