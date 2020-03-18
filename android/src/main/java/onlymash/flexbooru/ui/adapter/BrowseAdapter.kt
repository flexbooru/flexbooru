/*
 * Copyright (C) 2019. by onlymash <im@fiepi.me>, All rights reserved
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
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.net.toUri
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
import com.squareup.picasso.Picasso
import onlymash.flexbooru.common.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.entity.post.PostBase
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.decoder.CustomDecoder
import onlymash.flexbooru.decoder.CustomRegionDecoder
import onlymash.flexbooru.extension.isGifImage
import onlymash.flexbooru.extension.isStillImage
import onlymash.flexbooru.widget.DismissFrameLayout
import java.io.File
import java.util.concurrent.Executor

class BrowseAdapter(private val glideRequests: GlideRequests,
                    private val picasso: Picasso,
                    private val onDismissListener: DismissFrameLayout.OnDismissListener,
                    private val pageType: Int,
                    private val ioExecutor: Executor) : RecyclerView.Adapter<BrowseAdapter.BrowseViewHolder>() {

    private val size = Settings.browseSize
    private var posts: MutableList<PostBase> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowseViewHolder =
        BrowseViewHolder(DismissFrameLayout(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        })

    private var photoViewListener: PhotoViewListener? = null

    fun setPhotoViewListener(listener: PhotoViewListener) {
        photoViewListener = listener
    }

    interface PhotoViewListener {
        fun onClickPhotoView()
    }

    fun updateData(posts: MutableList<PostBase>) {
        this.posts = posts
        notifyDataSetChanged()
    }

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: BrowseViewHolder, position: Int) {
        val layout = holder.itemView as DismissFrameLayout
        layout.apply {
            setDismissListener(onDismissListener)
            removeAllViews()
            tag = position
        }
        val post = posts[position]
        val tranName = when (pageType) {
            Constants.PAGE_TYPE_POST -> layout.context.getString(R.string.post_transition_name, post.getPostId())
            Constants.PAGE_TYPE_POPULAR -> layout.context.getString(R.string.post_popular_transition_name, post.getPostId())
            else -> throw IllegalStateException("unknown post type $pageType")
        }
        val previewUrl = post.getPreviewUrl()
        val url = when (size) {
            Settings.POST_SIZE_SAMPLE -> post.getSampleUrl()
            Settings.POST_SIZE_LARGER -> post.getLargerUrl()
            else -> post.getOriginUrl()
        }
        if (url.isNotEmpty()) {
            when {
                url.isStillImage() -> {
                    val stillView = SubsamplingScaleImageView(layout.context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT)
                        transitionName = tranName
                        setOnClickListener {
                            photoViewListener?.onClickPhotoView()
                        }
                        setExecutor(ioExecutor)
                        setBitmapDecoderFactory { CustomDecoder(picasso) }
                        setRegionDecoderFactory { CustomRegionDecoder() }
                    }
                    val colorMatrix = ColorMatrix().apply {
                        setSaturation(0f)
                        set(floatArrayOf(
                            1f, 0f, 0f, 0f, 0f, // R
                            0f, 1f, 0f, 0f, 0f, // G
                            0f, 0f, 1f, 0f, 0f, // B
                            0f, 0f, 0f, 1f, 0f  // A
                        ))
                    }
                    val progressBar = ProgressBar(layout.context).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER)
                        indeterminateDrawable.colorFilter = ColorMatrixColorFilter(colorMatrix)
                    }
                    layout.apply {
                        removeAllViewsInLayout()
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
                    glideRequests.downloadOnly().load(url)
                        .into(object : CustomTarget<File>() {
                            override fun onLoadCleared(placeholder: Drawable?) {}
                            override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                                stillView.setImage(ImageSource.uri(resource.toUri()))
                            }
                        })
                }
                url.isGifImage() -> {
                    val gifView = PhotoView(layout.context).apply {
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        transitionName = tranName
                        setOnViewTapListener { _, _, _ ->
                            photoViewListener?.onClickPhotoView()
                        }
                    }
                    val colorMatrix = ColorMatrix().apply {
                        setSaturation(0f)
                        set(floatArrayOf(
                            1f, 0f, 0f, 0f, 0f, // R
                            0f, 1f, 0f, 0f, 0f, // G
                            0f, 0f, 1f, 0f, 0f, // B
                            0f, 0f, 0f, 1f, 0f  // A
                        ))
                    }
                    val progressBar = ProgressBar(layout.context).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER)
                        indeterminateDrawable.colorFilter = ColorMatrixColorFilter(colorMatrix)
                    }
                    layout.apply {
                        removeAllViewsInLayout()
                        addView(gifView, 0)
                        addView(progressBar, 1)
                    }
                    glideRequests.load(previewUrl)
                        .into(object : CustomTarget<Drawable>() {
                            override fun onLoadCleared(placeholder: Drawable?) {}
                            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                glideRequests
                                    .asGif()
                                    .load(url)
                                    .placeholder(resource)
                                    .addListener(object : RequestListener<GifDrawable> {
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
                                    })
                                    .into(gifView)
                            }
                        })
                }
                else -> {
                    val playerView = LayoutInflater.from(layout.context).inflate(R.layout.exoplayer, null) as PlayerView
                    playerView.tag = String.format("player_%d", position)
                    playerView.transitionName = tranName
                    layout.addView(playerView)
                }
            }
        }
    }

    override fun getItemCount(): Int = posts.size

    inner class BrowseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}