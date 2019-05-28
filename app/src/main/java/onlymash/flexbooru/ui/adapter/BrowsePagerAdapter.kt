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
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.net.toUri
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
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

class BrowsePagerAdapter(private val glideRequests: GlideRequests,
                         private val picasso: Picasso,
                         private val onDismissListener: DismissFrameLayout.OnDismissListener,
                         private val pageType: Int,
                         private val ioExecutor: Executor): PagerAdapter() {

    private val size = Settings.browseSize
    private var posts: MutableList<PostBase> = mutableListOf()

    @Suppress("UNCHECKED_CAST")
    fun updateData(posts: MutableList<PostBase>) {
        this.posts = posts
        notifyDataSetChanged()
    }
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int = posts.size

    @SuppressLint("InflateParams")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layout = DismissFrameLayout(container.context).apply {
            setDismissListener(onDismissListener)
            layoutParams = ViewPager.LayoutParams()
            tag = position
        }
        val post = posts[position]
        val tranName = when (pageType) {
            Constants.PAGE_TYPE_POST -> container.context.getString(R.string.post_transition_name, post.getPostId())
            Constants.PAGE_TYPE_POPULAR -> container.context.getString(R.string.post_popular_transition_name, post.getPostId())
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
                    val stillView = SubsamplingScaleImageView(container.context).apply {
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
                    val progressBar = ProgressBar(container.context).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER)
                        indeterminateDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
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
                    val gifView = PhotoView(container.context).apply {
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        transitionName = tranName
                        setOnViewTapListener { _, _, _ ->
                            photoViewListener?.onClickPhotoView()
                        }
                    }
                    val progressBar = ProgressBar(container.context).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER)
                        indeterminateDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
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
                    val playerView = LayoutInflater.from(container.context).inflate(R.layout.exoplayer, null) as PlayerView
                    playerView.tag = String.format("player_%d", position)
                    playerView.transitionName = tranName
                    layout.addView(playerView)
                }
            }
        }
        container.addView(layout)
        return layout
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    private var photoViewListener: PhotoViewListener? = null

    fun setPhotoViewListener(listener: PhotoViewListener) {
        photoViewListener = listener
    }

    interface PhotoViewListener {
        fun onClickPhotoView()
    }
}