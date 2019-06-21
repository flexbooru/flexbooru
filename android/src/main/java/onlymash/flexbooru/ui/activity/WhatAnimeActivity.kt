package onlymash.flexbooru.ui.activity

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.activity_what_anime.*
import kotlinx.android.synthetic.main.common_toolbar_list.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.R
import onlymash.flexbooru.extension.fileExt
import onlymash.flexbooru.extension.toVisibility
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.tracemoe.model.Doc
import onlymash.flexbooru.tracemoe.model.TraceResponse
import onlymash.flexbooru.tracemoe.presentation.TraceMoeActions
import onlymash.flexbooru.tracemoe.presentation.TraceMoePresenter
import onlymash.flexbooru.tracemoe.presentation.TraceMoeView
import java.io.ByteArrayOutputStream
import kotlin.properties.Delegates

private const val READ_IMAGE_REQUEST_CODE = 147

class WhatAnimeActivity : AppCompatActivity(), TraceMoeView {

    private var response: TraceResponse? = null

    private lateinit var whatAnimeAdapter: WhatAnimeAdapter

    @Suppress("EXPERIMENTAL_API_USAGE")
    private val actions: TraceMoeActions by lazy {
        TraceMoePresenter(Dispatchers.Main, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_what_anime)
        toolbar.apply {
            setTitle(R.string.title_what_anime)
            setNavigationOnClickListener {
                onBackPressed()
            }
        }
        whatAnimeAdapter = WhatAnimeAdapter()
        list.apply {
            layoutManager = LinearLayoutManager(this@WhatAnimeActivity, RecyclerView.VERTICAL, false)
            adapter = whatAnimeAdapter
        }
        what_anime_search_fab.setOnClickListener {
            try {
                startActivityForResult(
                    Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "image/*"
                    },
                    READ_IMAGE_REQUEST_CODE
                )
            } catch (_: ActivityNotFoundException) {}
        }
    }

    private fun search(uri: Uri) {
        response = null
        whatAnimeAdapter.notifyDataSetChanged()
        progress_bar.toVisibility(true)
        val ext = uri.toString().fileExt()
        val head = "data:image/jpeg;base64,"
        if (ext == "gif" || ext == "GIF") {
            GlideApp.with(this)
                .asGif()
                .load(uri)
                .into(object : CustomTarget<GifDrawable>() {
                    override fun onLoadCleared(placeholder: Drawable?) {
                        progress_bar.toVisibility(false)
                    }
                    override fun onResourceReady(resource: GifDrawable, transition: Transition<in GifDrawable>?) {
                        val bitmap = resource.firstFrame
                        if (bitmap != null) {
                            lifecycleScope.launch {
                                val encodedImage = withContext(Dispatchers.IO) {
                                    try {
                                        val os = ByteArrayOutputStream()
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, if (bitmap.width > 1000 || bitmap.height > 1000) 50 else 100, os)
                                        Base64.encodeToString(os.toByteArray(), Base64.DEFAULT)
                                    } catch (_: Exception) {
                                        null
                                    }
                                }
                                if (encodedImage != null) {
                                    actions.onRequestData(head + encodedImage)
                                } else {
                                    progress_bar.toVisibility(false)
                                }
                            }
                        } else progress_bar.toVisibility(false)
                    }
                })
        } else {
            lifecycleScope.launch {
                val encodedImage = withContext(Dispatchers.IO) {
                    try {
                        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
                        } else {
                            @Suppress("DEPRECATION")
                            MediaStore.Images.Media.getBitmap(contentResolver, uri)
                        } ?: return@withContext null
                        val os = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, if (bitmap.width > 1000 || bitmap.height > 1000) 50 else 100, os)
                        Base64.encodeToString(os.toByteArray(), Base64.DEFAULT)
                    } catch (e: Exception) {
                        null
                    }
                }
                if (encodedImage != null) {
                    actions.onRequestData(head + encodedImage)
                } else {
                    progress_bar.toVisibility(false)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == READ_IMAGE_REQUEST_CODE) {
            data?.data?.also {
                search(it)
            }
        }
    }

    override var isUpdating: Boolean by Delegates.observable(false) { _, _, isLoading ->
        progress_bar.toVisibility(isLoading)
        if (isLoading) {
            error_msg.toVisibility(false)
        }
    }

    override fun onUpdate(data: TraceResponse) {
        response = data
        whatAnimeAdapter.notifyDataSetChanged()
    }

    override fun showError(error: Throwable) {
        error_msg.toVisibility(true)
        error_msg.text = error.message ?: ""
    }

    inner class WhatAnimeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            WhatAnimeViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_what_anime, parent, false))

        override fun getItemCount(): Int = response?.docs?.size ?: 0

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val result = response?.docs?.get(position) ?: return
            (holder as WhatAnimeViewHolder).bind(result)
        }

        inner class WhatAnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val preview: AppCompatImageView = itemView.findViewById(R.id.preview)
            private val title: AppCompatTextView = itemView.findViewById(R.id.title)
            private val info1: AppCompatTextView = itemView.findViewById(R.id.info_1)
            private val info2: AppCompatTextView = itemView.findViewById(R.id.info_2)

            fun bind(data: Doc) {
                title.text = data.title ?: data.filename
                info1.text = formatTime(data.at)
                info2.text = data.anime
                val previewUrl = "https://trace.moe/thumbnail.php?anilist_id=${data.anilistId}&file=${Uri.encode(data.filename)}&t=${data.at}&token=${data.tokenthumb}"
                GlideApp.with(itemView.context)
                    .load(previewUrl)
                    .fitCenter()
                    .into(preview)
            }

            private fun formatTime(time: Float): String {
                val minute = (time / 60).toInt()
                val second = (time % 60).toInt()
                val decimal = ((time % 1) * 100).toInt()
                return timeString(minute) + ":" + timeString(second) + "." + timeString(decimal)
            }

            private fun timeString(time: Int): String {
                return if (time < 10) "0$time" else time.toString()
            }
        }
    }
}
