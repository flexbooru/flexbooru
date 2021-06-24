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

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Settings.isOrderSuccess
import onlymash.flexbooru.app.Settings.safeMode
import onlymash.flexbooru.databinding.ActivityWhatAnimeBinding
import onlymash.flexbooru.databinding.FragmentAnimePlayerBinding
import onlymash.flexbooru.databinding.ItemWhatAnimeBinding
import onlymash.flexbooru.common.di.diCommon
import onlymash.flexbooru.exoplayer.PlayerHolder
import onlymash.flexbooru.extension.copyText
import onlymash.flexbooru.extension.fileExt
import onlymash.flexbooru.extension.toVisibility
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.common.tracemoe.api.TraceMoeApi
import onlymash.flexbooru.common.tracemoe.model.Doc
import onlymash.flexbooru.ui.base.BaseBottomSheetDialog
import onlymash.flexbooru.ui.viewmodel.TraceMoeViewModel
import onlymash.flexbooru.ui.viewmodel.getTraceMoeViewModel
import onlymash.flexbooru.ui.base.BaseActivity
import onlymash.flexbooru.ui.helper.OpenFileLifecycleObserver
import onlymash.flexbooru.ui.viewbinding.viewBinding
import org.kodein.di.instance
import java.io.ByteArrayOutputStream

private const val PREVIEW_VIDEO_URL_KEY = "preview_video_url"

class WhatAnimeActivity : BaseActivity() {

    private val docs: MutableList<Doc> = mutableListOf()

    private lateinit var whatAnimeAdapter: WhatAnimeAdapter
    private lateinit var traceMoeViiewModel: TraceMoeViewModel
    private lateinit var openFileObserver: OpenFileLifecycleObserver

    private val api by diCommon.instance<TraceMoeApi>("TraceMoeApi")

    private val binding by viewBinding(ActivityWhatAnimeBinding::inflate)
    private val progressBar get() = binding.common.progress.progressBar
    private val errorMsg get() = binding.common.errorMsg
        
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isOrderSuccess) {
            startActivity(Intent(this, PurchaseActivity::class.java))
            finish()
            return
        }
        setContentView(binding.root)
        val list = binding.common.list
        val fab = binding.fab
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
        traceMoeViiewModel.data.observe(this, { response ->
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
        traceMoeViiewModel.isLoading.observe(this, {
            progressBar.isVisible = it
            if (it && errorMsg.isVisible) {
                errorMsg.isVisible = false
            }
        })
        traceMoeViiewModel.error.observe(this, {
            if (!it.isNullOrBlank()) {
                errorMsg.isVisible = true
                errorMsg.text = it
            } else {
                errorMsg.isVisible = false
            }
        })
        openFileObserver = OpenFileLifecycleObserver(activityResultRegistry) { uri ->
            search(uri)
        }
        lifecycle.addObserver(openFileObserver)
        fab.setOnClickListener {
            openFileObserver.openDocument("image/*")
        }
    }

    private fun search(uri: Uri) {
        docs.clear()
        whatAnimeAdapter.notifyDataSetChanged()
        progressBar.toVisibility(true)
        val ext = uri.toString().fileExt()
        val head = "data:image/jpeg;base64,"
        if (ext == "gif" || ext == "GIF") {
            GlideApp.with(this)
                .asGif()
                .load(uri)
                .into(object : CustomTarget<GifDrawable>() {
                    override fun onLoadCleared(placeholder: Drawable?) {
                        progressBar.toVisibility(false)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    inner class WhatAnimeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int): RecyclerView.ViewHolder = WhatAnimeViewHolder(parent)

        override fun getItemCount(): Int = docs.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as WhatAnimeViewHolder).bind(docs[position])
        }

        inner class WhatAnimeViewHolder(binding: ItemWhatAnimeBinding) : RecyclerView.ViewHolder(binding.root) {

            constructor(parent: ViewGroup): this(parent.viewBinding(ItemWhatAnimeBinding::inflate))

            private val preview = binding.preview
            private val title = binding.title
            private val info1 = binding.info1
            private val info2 = binding.info2

            private lateinit var data: Doc

            init {
                itemView.setOnClickListener {
                    showPlayerDialog()
                }
                itemView.setOnLongClickListener {
                    copyText(title.text)
                    true
                }
            }

            private fun showPlayerDialog() {
                if (isFinishing) {
                    return
                }
                AnimePlayerDialog().apply {
                    arguments = Bundle().apply {
                        putString(PREVIEW_VIDEO_URL_KEY, "https://media.trace.moe/video/${data.anilistId}/${Uri.encode(data.filename)}?t=${data.at}&token=${data.tokenthumb}")
                    }
                    show(supportFragmentManager, "player")
                }
            }

            fun bind(data: Doc) {
                this.data = data
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

    class AnimePlayerDialog : BaseBottomSheetDialog() {

        private var playerView: PlayerView? = null
        private lateinit var behavior: BottomSheetBehavior<View>
        private lateinit var binding: FragmentAnimePlayerBinding

        private var url: String? = null

        private val playerHolder: PlayerHolder by lazy { PlayerHolder() }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            url = arguments?.getString(PREVIEW_VIDEO_URL_KEY)
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val dialog = super.onCreateDialog(savedInstanceState)
            binding = FragmentAnimePlayerBinding.inflate(layoutInflater)
            playerView = binding.exoplayerView
            dialog.setContentView(binding.root)
            behavior = BottomSheetBehavior.from(binding.root.parent as View)
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
                val playerView = playerView
                val uri = url?.toUri()
                if (playerView != null && uri != null) {
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
            playerView?.onPause()
            playerView?.player = null
            playerHolder.release()
        }
    }
}
