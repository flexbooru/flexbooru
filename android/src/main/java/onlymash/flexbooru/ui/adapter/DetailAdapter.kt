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
import com.squareup.picasso.Picasso
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings.POST_SIZE_LARGER
import onlymash.flexbooru.common.Settings.POST_SIZE_SAMPLE
import onlymash.flexbooru.common.Settings.detailSize
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.decoder.CustomDecoder
import onlymash.flexbooru.decoder.CustomRegionDecoder
import onlymash.flexbooru.extension.isGifImage
import onlymash.flexbooru.extension.isStillImage
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.widget.DismissFrameLayout
import java.io.File
import java.util.concurrent.Executor

class DetailAdapter(
    private val glide: GlideRequests,
    private val picasso: Picasso,
    private val dismissListener: DismissFrameLayout.OnDismissListener,
    private val ioExecutor: Executor,
    private val clickCallback: () -> Unit) : PagedListAdapter<Post, RecyclerView.ViewHolder>(PostAdapter.POST_COMPARATOR) {

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

    fun getPost(position: Int) = getItem(position)

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val layout = holder.itemView as DismissFrameLayout
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
            url.isStillImage() -> {
                val stillView = SubsamplingScaleImageView(layout.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
                    setOnClickListener {
                        clickCallback()
                    }
                    setExecutor(ioExecutor)
                    setBitmapDecoderFactory { CustomDecoder(picasso) }
                    setRegionDecoderFactory { CustomRegionDecoder() }
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
            url.isGifImage() -> {
                val gifView = PhotoView(layout.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    setOnViewTapListener { _, _, _ ->
                        clickCallback()
                    }
                }
                val progressBar = ProgressBar(layout.context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER)
                    indeterminateDrawable.colorFilter = ColorMatrixColorFilter(colorMatrix)
                }
                layout.apply {
                    addView(gifView, 0)
                    addView(progressBar, 1)
                }
                glide.load(post.preview)
                    .into(object : CustomTarget<Drawable>() {
                        override fun onLoadCleared(placeholder: Drawable?) {}
                        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                            glide.asGif()
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
                layout.addView(playerView)
            }
        }
    }
}