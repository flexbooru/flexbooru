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
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import com.google.android.material.textview.MaterialTextView
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.ui.PlayerView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.imageLoader
import coil.load
import coil.request.ImageRequest
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.chrisbanes.photoview.PhotoView
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Settings.POST_SIZE_LARGER
import onlymash.flexbooru.app.Settings.POST_SIZE_ORIGIN
import onlymash.flexbooru.app.Settings.POST_SIZE_SAMPLE
import onlymash.flexbooru.app.Settings.detailSize
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.decoder.CustomDecoder
import onlymash.flexbooru.decoder.CustomRegionDecoder
import onlymash.flexbooru.extension.fileExt
import onlymash.flexbooru.extension.isGifImage
import onlymash.flexbooru.extension.isImage
import onlymash.flexbooru.extension.isVideo
import onlymash.flexbooru.widget.DismissFrameLayout
import java.util.concurrent.Executor

class DetailAdapter(
    private val dismissListener: DismissFrameLayout.OnDismissListener,
    private val ioExecutor: Executor,
    private val clickCallback: () -> Unit,
    private val longClickCallback: () -> Unit) : PagingDataAdapter<Post, RecyclerView.ViewHolder>(DETAIL_POST_COMPARATOR) {

    companion object {
        val DETAIL_POST_COMPARATOR = object : DiffUtil.ItemCallback<Post>() {
            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem.id == newItem.id
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem.booruUid == newItem.booruUid &&
                        oldItem.query == newItem.query &&
                        oldItem.id == newItem.id
        }
    }

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
        }) { }
    }

    fun getPost(position: Int): Post? {
        return if (position in 0 until itemCount) getItem(position) else null
    }

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val layout = holder.itemView as FrameLayout
        if (layout.childCount > 0) {
            layout.removeAllViews()
        }
        val post = getItem(position) ?: return
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
                val textView = LayoutInflater.from(layout.context).inflate(R.layout.item_unsupported_format, null) as MaterialTextView
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
            setBitmapDecoderFactory { CustomDecoder() }
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
        val request = ImageRequest.Builder(stillView.context)
            .data(url)
            .target {
                stillView.setImage(ImageSource.bitmap(it.toBitmap()))
            }
            .build()
        stillView.context.imageLoader.enqueue(request)
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
            val imageLoader = photoView.context.imageLoader.newBuilder()
                .components {
                    if (Build.VERSION.SDK_INT >= 28) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                }
                .build()
            val request = ImageRequest.Builder(photoView.context)
                .data(url)
                .target(photoView)
                .listener(
                    onError = { _, _ ->
                        layout.removeView(progressBar)
                    },
                    onSuccess = {  _, _ ->
                        layout.removeView(progressBar)
                    }
                )
                .build()
            imageLoader.enqueue(request)
        } else {
            photoView.load(url) {
                listener(
                    onError = { _, _ ->
                        layout.removeView(progressBar)
                    },
                    onSuccess = {  _, _ ->
                        layout.removeView(progressBar)
                    }
                )
            }
        }
    }
}