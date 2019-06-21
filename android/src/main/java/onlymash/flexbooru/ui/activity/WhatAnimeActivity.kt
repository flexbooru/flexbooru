package onlymash.flexbooru.ui.activity

import android.app.Activity
import android.app.Dialog
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
import android.view.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_what_anime.*
import kotlinx.android.synthetic.main.common_toolbar_list.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.exoplayer.PlayerHolder
import onlymash.flexbooru.extension.copyText
import onlymash.flexbooru.extension.fileExt
import onlymash.flexbooru.extension.toVisibility
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.tracemoe.model.Doc
import onlymash.flexbooru.tracemoe.model.TraceResponse
import onlymash.flexbooru.tracemoe.presentation.TraceMoeActions
import onlymash.flexbooru.tracemoe.presentation.TraceMoePresenter
import onlymash.flexbooru.tracemoe.presentation.TraceMoeView
import onlymash.flexbooru.ui.fragment.TransparentBottomSheetDialogFragment
import java.io.ByteArrayOutputStream
import kotlin.properties.Delegates

private const val READ_IMAGE_REQUEST_CODE = 147
private const val PREVIEW_VIDEO_URL_KEY = "preview_video_url"

class WhatAnimeActivity : AppCompatActivity(), TraceMoeView {

    private val docs: MutableList<Doc> = mutableListOf()

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
        if (!Settings.isOrderSuccess) {
            startActivity(Intent(this, PurchaseActivity::class.java))
            finish()
        }
    }

    private fun search(uri: Uri) {
        docs.clear()
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
        docs.clear()
        if (Settings.safeMode) {
            data.docs.forEach {
                if (!it.isAdult) {
                    docs.add(it)
                }
            }
        } else {
            docs.addAll(data.docs)
        }
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

        override fun getItemCount(): Int = docs.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as WhatAnimeViewHolder).bind(docs[position])
        }

        inner class WhatAnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val preview: AppCompatImageView = itemView.findViewById(R.id.preview)
            private val title: AppCompatTextView = itemView.findViewById(R.id.title)
            private val info1: AppCompatTextView = itemView.findViewById(R.id.info_1)
            private val info2: AppCompatTextView = itemView.findViewById(R.id.info_2)

            fun bind(data: Doc) {
                val text = data.title ?: data.filename
                title.text = text
                info1.text = formatTime(data.at)
                info2.text = data.anime
                val previewUrl = "https://trace.moe/thumbnail.php?anilist_id=${data.anilistId}&file=${Uri.encode(data.filename)}&t=${data.at}&token=${data.tokenthumb}"
                val placeholderId = if (data.isAdult) R.drawable.background_rating_e else R.drawable.background_rating_s
                GlideApp.with(itemView.context)
                    .load(previewUrl)
                    .placeholder(ContextCompat.getDrawable(itemView.context, placeholderId))
                    .fitCenter()
                    .into(preview)
                itemView.setOnLongClickListener {
                    copyText(text)
                    true
                }
                itemView.setOnClickListener {
                    AnimePlayerDialog().apply {
                        arguments = Bundle().apply {
                            putString(PREVIEW_VIDEO_URL_KEY, "https://media.trace.moe/video/${data.anilistId}/${Uri.encode(data.filename)}?t=${data.at}&token=${data.tokenthumb}")
                        }
                        show(supportFragmentManager, "player")
                    }
                }
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

    class AnimePlayerDialog : TransparentBottomSheetDialogFragment() {

        private lateinit var playerView: PlayerView
        private lateinit var behavior: BottomSheetBehavior<View>

        private var url: String? = null

        private val playerHolder: PlayerHolder by lazy { PlayerHolder(requireContext()) }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            url = arguments?.getString(PREVIEW_VIDEO_URL_KEY)
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val dialog = super.onCreateDialog(savedInstanceState)
            val view = View.inflate(context, R.layout.fragment_anime_player, null)
            playerView = view.findViewById(R.id.exoplayer_view)
            dialog.setContentView(view)
            behavior = BottomSheetBehavior.from(view.parent as View)
            behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        dismiss()
                    }
                }
            })
            return dialog
        }

        override fun onResume() {
            super.onResume()
            url?.let {
                playerHolder.start(Uri.parse(it), playerView)
            }
        }

        override fun onStart() {
            super.onStart()
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        override fun onStop() {
            super.onStop()
            playerHolder.stop()
        }

        override fun onDestroy() {
            super.onDestroy()
            playerHolder.release()
        }
    }
}
