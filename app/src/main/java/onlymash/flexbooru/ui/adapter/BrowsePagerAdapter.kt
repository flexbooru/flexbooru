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
import android.graphics.drawable.Drawable
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.exoplayer2.ui.PlayerView
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.entity.PostDan
import onlymash.flexbooru.entity.PostMoe
import onlymash.flexbooru.util.isImage

class BrowsePagerAdapter(private val glideRequests: GlideRequests): PagerAdapter() {

    private var type = -1
    private var postsDan: MutableList<PostDan> = mutableListOf()
    private var postsMoe: MutableList<PostMoe> = mutableListOf()

    private val uiHandler = Handler()

    @Suppress("UNCHECKED_CAST")
    fun updateData(posts: Any, type: Int) {
        this.type = type
        if (type == Constants.TYPE_DANBOORU) {
            postsDan = posts as MutableList<PostDan>
            postsMoe = mutableListOf()
        } else {
            postsMoe = posts as MutableList<PostMoe>
            postsDan = mutableListOf()
        }
        notifyDataSetChanged()
    }
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return if (type == Constants.TYPE_DANBOORU) postsDan.size else postsMoe.size
    }

    @SuppressLint("InflateParams")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(container.context).inflate(R.layout.item_post_pager, null)
        view.tag = position
        val photoView: PhotoView = view.findViewById(R.id.photo_view)
        photoView.setOnViewTapListener { _, _, _ ->
            photoViewListener?.onClickPhotoView()
        }
        var previewUrl = ""
        val url = when (type) {
            Constants.TYPE_DANBOORU -> {
                photoView.transitionName = String.format(container.context.getString(R.string.post_transition_name), postsDan[position].id)
                previewUrl = postsDan[position].preview_file_url!!
                postsDan[position].large_file_url
            }
            Constants.TYPE_MOEBOORU -> {
                photoView.transitionName = String.format(container.context.getString(R.string.post_transition_name), postsMoe[position].id)
                previewUrl = postsMoe[position].preview_url
                postsMoe[position].sample_url
            }
            else -> ""
        }
        if (!url.isNullOrBlank()) {
            when {
                url.isImage() -> {
                    glideRequests
                        .load(previewUrl)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                uiHandler.post {
                                    glideRequests
                                        .load(url)
                                        .into(photoView)
                                }
                                return true
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                uiHandler.post {
                                    glideRequests
                                        .load(url)
                                        .placeholder(resource)
                                        .into(photoView)
                                }
                                return true
                            }

                        })
                        .into(photoView)
                }
                else -> {
                    val playerView: PlayerView = view.findViewById(R.id.player_view)
                    playerView.visibility = View.VISIBLE
                    playerView.tag = String.format("player_%d", position)
                }
            }
        }
        container.addView(view)
        return view
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