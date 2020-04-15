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
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_what_anime.*
import kotlinx.android.synthetic.main.common_list.*
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings.isOrderSuccess
import onlymash.flexbooru.common.Settings.safeMode
import onlymash.flexbooru.exoplayer.PlayerHolder
import onlymash.flexbooru.extension.copyText
import onlymash.flexbooru.extension.fileExt
import onlymash.flexbooru.extension.toVisibility
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.tracemoe.api.TraceMoeApi
import onlymash.flexbooru.tracemoe.di.kodeinTraceMoe
import onlymash.flexbooru.tracemoe.model.Doc
import onlymash.flexbooru.ui.fragment.BaseBottomSheetDialogFragment
import onlymash.flexbooru.ui.viewmodel.TraceMoeViewModel
import onlymash.flexbooru.ui.viewmodel.getTraceMoeViewModel
import onlymash.flexbooru.widget.drawNavBar
import org.kodein.di.erased.instance
import java.io.ByteArrayOutputStream

private const val READ_IMAGE_REQUEST_CODE = 147
private const val PREVIEW_VIDEO_URL_KEY = "preview_video_url"

class WhatAnimeActivity : AppCompatActivity() {

    private val docs: MutableList<Doc> = mutableListOf()

    private lateinit var whatAnimeAdapter: WhatAnimeAdapter
    private lateinit var traceMoeViiewModel: TraceMoeViewModel

    private val api by kodeinTraceMoe.instance<TraceMoeApi>("TraceMoeApi")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isOrderSuccess) {
            startActivity(Intent(this, PurchaseActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_what_anime)
        drawNavBar {
            list.updatePadding(bottom = it.systemWindowInsetBottom)
            what_anime_search_fab.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                bottomMargin = it.systemWindowInsetBottom +
                        resources.getDimensionPixelSize(R.dimen.margin_normal)
            }
        }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.title_what_anime)
        }
        whatAnimeAdapter = WhatAnimeAdapter()
        list.apply {
            layoutManager = LinearLayoutManager(this@WhatAnimeActivity, RecyclerView.VERTICAL, false)
            adapter = whatAnimeAdapter
        }
        traceMoeViiewModel = getTraceMoeViewModel(api)
        traceMoeViiewModel.data.observe(this, Observer { response ->
            docs.clear()
            if (response != null) {
                if (safeMode) {
                    response.docs.forEach {
                        if (!it.isAdult) {
                            docs.add(it)
                        }
                    }
                } else {
                    docs.addAll(response.docs)
                }
            }
            whatAnimeAdapter.notifyDataSetChanged()
        })
        traceMoeViiewModel.isLoading.observe(this, Observer {
            progress_bar.isVisible = it
            if (it && error_msg.isVisible) {
                error_msg.isVisible = false
            }
        })
        traceMoeViiewModel.error.observe(this, Observer {
            if (!it.isNullOrBlank()) {
                error_msg.isVisible = true
                error_msg.text = it
            } else {
                error_msg.isVisible = false
            }
        })
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
                                    traceMoeViiewModel.fetch(head + encodedImage)
                                }
                            }
                        }
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
                    traceMoeViiewModel.fetch(head + encodedImage)
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

    class AnimePlayerDialog : BaseBottomSheetDialogFragment() {

        private lateinit var playerView: PlayerView
        private lateinit var behavior: BottomSheetBehavior<View>

        private var url: String? = null

        private val playerHolder: PlayerHolder by lazy { PlayerHolder() }

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
            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
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
            context?.apply {
                playerHolder.create(applicationContext)
                url?.toUri()?.let { uri ->
                    playerHolder.start(applicationContext, uri, playerView)
                }
            }
        }

        override fun onStart() {
            super.onStart()
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        override fun onStop() {
            super.onStop()
            playerView.onPause()
            playerView.player = null
            playerHolder.release()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
