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

import android.annotation.SuppressLint
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.IntRange
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.exoplayer2.ui.PlayerView
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings.POST_SIZE_LARGER
import onlymash.flexbooru.common.Settings.POST_SIZE_ORIGIN
import onlymash.flexbooru.common.Settings.POST_SIZE_SAMPLE
import onlymash.flexbooru.common.Settings.detailSize
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.decoder.CustomDecoder
import onlymash.flexbooru.decoder.CustomRegionDecoder
import onlymash.flexbooru.extension.fileExt
import onlymash.flexbooru.extension.isGifImage
import onlymash.flexbooru.extension.isImage
import onlymash.flexbooru.extension.isVideo
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.widget.DismissFrameLayout
import java.io.File
import java.util.concurrent.Executor

class DetailAdapter(
    private val glide: GlideRequests,
    private val dismissListener: DismissFrameLayout.OnDismissListener,
    private val ioExecutor: Executor,
    private val clickCallback: () -> Unit,
    private val longClickCallback: () -> Unit) : PagedListAdapter<Post, RecyclerView.ViewHolder>(PostAdapter.POST_COMPARATOR) {

    private val size = detailSize
    private val colorMatrix = ColorMatrix().apply {
        setSaturation(0f)
        set(floatArrayOf(
            1f, 0f, 0f, 0f, 0f, // R
            0f, 1f, 0f, 0f, 0f, // G
            0f, 0f, 1f, 0f, 0f, // B
            0f, 0f, 0f, 1f, 0f  // A
        ))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return object : RecyclerView.ViewHolder(DismissFrameLayout(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setDismissListener(dismissListener)
        }) {

        }
    }

    fun getPost(@IntRange(from = 0) position: Int) = try {
        getItem(position)
    } catch (_: IndexOutOfBoundsException) {
        null
    }

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val layout = holder.itemView as FrameLayout
        if (layout.childCount > 0) {
            layout.removeAllViews()
        }
        val post = getPost(position) ?: return
        val url = when (size) {
            POST_SIZE_SAMPLE -> post.sample
            POST_SIZE_LARGER -> post.medium
            else -> post.origin
        }
        when {
            url.isImage() -> {
                when {
                    url.isGifImage() -> {
                        loadSampleOrGifImage(layout, post, url, true)
                    }
                    size == POST_SIZE_ORIGIN -> {
                        loadStillImage(layout, post.id, url)
                    }
                    else -> {
                        loadSampleOrGifImage(layout, post, url, false)
                    }
                }
            }
            url.isVideo() -> {
                val playerView = LayoutInflater.from(layout.context).inflate(R.layout.item_exoplayer, null) as PlayerView
                playerView.apply {
                    tag = String.format("player_%d", position)
                    transitionName = String.format("post_%d", post.id)
                    setOnClickListener {
                        clickCallback()
                    }
                }
                layout.addView(playerView)
            }
            else -> {
                val textView = LayoutInflater.from(layout.context).inflate(R.layout.item_unsupported_format, null) as AppCompatTextView
                textView.text = String.format(layout.context.getString(R.string.browse_unsupported_format), url.fileExt())
                textView.transitionName = String.format("post_%d", post.id)
                layout.addView(textView)
            }
        }
    }

    private fun loadStillImage(layout: FrameLayout, postId: Int, url: String) {
        val stillView = SubsamplingScaleImageView(layout.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
            setOnClickListener {
                clickCallback()
            }
            setOnLongClickListener {
                longClickCallback()
                false
            }
            setExecutor(ioExecutor)
            setBitmapDecoderFactory { CustomDecoder(glide) }
            setRegionDecoderFactory { CustomRegionDecoder() }
            transitionName = String.format("post_%d", postId)
        }
        val progressBar = ProgressBar(layout.context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER)
            indeterminateDrawable.colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        layout.apply {
            addView(stillView, 0)
            addView(progressBar, 1)
        }
        stillView.setOnImageEventListener(object : SubsamplingScaleImageView.OnImageEventListener {
            override fun onImageLoaded() {
                layout.removeView(progressBar)
            }
            override fun onReady() {}
            override fun onTileLoadError(e: Exception?) {}
            override fun onPreviewReleased() {}
            override fun onImageLoadError(e: Exception?) {}
            override fun onPreviewLoadError(e: Exception?) {}
        })
        glide.downloadOnly().load(url)
            .into(object : CustomTarget<File>() {
                override fun onLoadCleared(placeholder: Drawable?) {}
                override fun onResourceReady(
                    resource: File,
                    transition: Transition<in File>?) {
                    stillView.setImage(ImageSource.uri(resource.toUri()))
                }
            })
    }

    private fun loadSampleOrGifImage(layout: FrameLayout, post: Post, url: String, isGif: Boolean) {
        val photoView = PhotoView(layout.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            isClickable = true
            setOnViewTapListener { _, _, _ ->
                clickCallback()
            }
            setOnLongClickListener {
                longClickCallback()
                false
            }
            transitionName = String.format("post_%d", post.id)
        }
        val progressBar = ProgressBar(layout.context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER)
            indeterminateDrawable.colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        layout.apply {
            addView(photoView, 0)
            addView(progressBar, 1)
        }
        if (isGif) {
            val gifListener = object : RequestListener<GifDrawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    layout.removeView(progressBar)
                    return false
                }
                override fun onResourceReady(
                    resource: GifDrawable?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    layout.removeView(progressBar)
                    return false
                }
            }
            glide.load(post.preview)
                .into(object : CustomTarget<Drawable>() {
                    override fun onLoadCleared(placeholder: Drawable?) {}
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        glide.asGif()
                            .load(url)
                            .placeholder(resource)
                            .addListener(gifListener)
                            .into(photoView)
                    }
                })
        } else {
            val stillListener = object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    layout.removeView(progressBar)
                    return false
                }
                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    layout.removeView(progressBar)
                    return false
                }
            }
            glide.load(url)
                .addListener(stillListener)
                .into(photoView)
        }
    }
}