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
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.exoplayer2.ui.PlayerView
import com.squareup.picasso.Picasso
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.ServiceLocator
import onlymash.flexbooru.Settings
import onlymash.flexbooru.entity.PostDan
import onlymash.flexbooru.entity.PostDanOne
import onlymash.flexbooru.entity.PostMoe
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.util.image.CustomDecoder
import onlymash.flexbooru.util.image.CustomRegionDecoder
import onlymash.flexbooru.util.isGifImage
import onlymash.flexbooru.util.isStillImage
import onlymash.flexbooru.widget.DismissFrameLayout
import java.io.File

class BrowsePagerAdapter(private val glideRequests: GlideRequests,
                         private val onDismissListener: DismissFrameLayout.OnDismissListener,
                         private val pageType: Int): PagerAdapter() {

    private var type = -1
    private val size = Settings.instance().browseSize
    private var postsDan: MutableList<PostDan> = mutableListOf()
    private var postsDanOne: MutableList<PostDanOne> = mutableListOf()
    private var postsMoe: MutableList<PostMoe> = mutableListOf()

    @Suppress("UNCHECKED_CAST")
    fun updateData(posts: Any, type: Int) {
        this.type = type
        when (type) {
            Constants.TYPE_DANBOORU -> postsDan = posts as MutableList<PostDan>
            Constants.TYPE_DANBOORU_ONE -> postsDanOne = posts as MutableList<PostDanOne>
            else -> postsMoe = posts as MutableList<PostMoe>
        }
        notifyDataSetChanged()
    }
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int = when (type) {
        Constants.TYPE_DANBOORU -> postsDan.size
        Constants.TYPE_DANBOORU_ONE -> postsDanOne.size
        else -> postsMoe.size
    }

    @SuppressLint("InflateParams")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layout = DismissFrameLayout(container.context).apply {
            setDismissListener(onDismissListener)
            layoutParams = ViewPager.LayoutParams()
            tag = position
        }
        var tranName = ""
        var previewUrl = ""
        var url = ""
        when (type) {
            Constants.TYPE_DANBOORU -> {
                tranName = when (pageType) {
                    Constants.PAGE_TYPE_POST -> container.context.getString(R.string.post_transition_name, postsDan[position].id)
                    Constants.PAGE_TYPE_POPULAR -> container.context.getString(R.string.post_popular_transition_name, postsDan[position].id)
                    else -> throw IllegalStateException("unknown post type $pageType")
                }
                previewUrl = postsDan[position].getPreviewUrl()
                url = when (size) {
                    Settings.POST_SIZE_SAMPLE -> postsDan[position].getSampleUrl()
                    Settings.POST_SIZE_LARGER -> postsDan[position].getLargerUrl()
                    else -> postsDan[position].getOriginUrl()
                }
            }
            Constants.TYPE_MOEBOORU -> {
                tranName = when (pageType) {
                    Constants.PAGE_TYPE_POST -> container.context.getString(R.string.post_transition_name, postsMoe[position].id)
                    Constants.PAGE_TYPE_POPULAR -> container.context.getString(R.string.post_popular_transition_name, postsMoe[position].id)
                    else -> throw IllegalStateException("unknown post type $pageType")
                }
                previewUrl = postsMoe[position].getPreviewUrl()
                url = when (size) {
                    Settings.POST_SIZE_SAMPLE -> postsMoe[position].getSampleUrl()
                    Settings.POST_SIZE_LARGER -> postsMoe[position].getLargerUrl()
                    else -> postsMoe[position].getOriginUrl()
                }
            }
            Constants.TYPE_DANBOORU_ONE -> {
                tranName = when (pageType) {
                    Constants.PAGE_TYPE_POST -> container.context.getString(R.string.post_transition_name, postsDanOne[position].id)
                    Constants.PAGE_TYPE_POPULAR -> container.context.getString(R.string.post_popular_transition_name, postsDanOne[position].id)
                    else -> throw IllegalStateException("unknown post type $pageType")
                }
                previewUrl = postsDanOne[position].getPreviewUrl()
                url = when (size) {
                    Settings.POST_SIZE_SAMPLE -> postsDanOne[position].getSampleUrl()
                    Settings.POST_SIZE_LARGER -> postsDanOne[position].getLargerUrl()
                    else -> postsDanOne[position].getOriginUrl()
                }
            }
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
                        setExecutor(ServiceLocator.instance().getDiskIOExecutor())
                        setBitmapDecoderFactory { CustomDecoder(Picasso.Builder(context).build()) }
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
                    layout.removeAllViewsInLayout()
                    val gifView = PhotoView(container.context).apply {
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        transitionName = tranName
                        setOnViewTapListener { _, _, _ ->
                            photoViewListener?.onClickPhotoView()
                        }
                    }
                    layout.addView(gifView)
                    glideRequests.load(previewUrl)
                        .into(object : CustomTarget<Drawable>() {
                            override fun onLoadCleared(placeholder: Drawable?) {}
                            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                glideRequests
                                    .asGif()
                                    .load(url)
                                    .placeholder(resource)
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