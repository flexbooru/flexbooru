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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.dekoservidoni.omfm.OneMoreFabMenu
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Settings.isOrderSuccess
import onlymash.flexbooru.app.Settings.sauceNaoApiKey
import onlymash.flexbooru.databinding.ActivitySauceNaoBinding
import onlymash.flexbooru.databinding.ItemSauceNaoBinding
import onlymash.flexbooru.common.di.diCommon
import onlymash.flexbooru.extension.*
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.common.saucenao.api.SauceNaoApi
import onlymash.flexbooru.common.saucenao.model.Result
import onlymash.flexbooru.common.saucenao.model.SauceNaoResponse
import onlymash.flexbooru.ui.viewmodel.SauceNaoViewModel
import onlymash.flexbooru.ui.viewmodel.getSauceNaoViewModel
import onlymash.flexbooru.ui.base.BaseActivity
import onlymash.flexbooru.ui.helper.OpenFileLifecycleObserver
import onlymash.flexbooru.ui.viewbinding.viewBinding
import org.kodein.di.instance
import java.io.IOException

const val SAUCE_NAO_SEARCH_URL_KEY = "sauce_nao_search_url"

class SauceNaoActivity : BaseActivity() {

    companion object {
        fun startSearch(context: Context, url: String) {
            context.startActivity(
                Intent(context, SauceNaoActivity::class.java).apply {
                    putExtra(SAUCE_NAO_SEARCH_URL_KEY, url)
                }
            )
        }
    }

    private val api by diCommon.instance<SauceNaoApi>("SauceNaoApi")
    private val binding by viewBinding(ActivitySauceNaoBinding::inflate)
    private val fab get() = binding.sauceNaoSearchFab
    private val errorMsg get() = binding.common.errorMsg

    private var response: SauceNaoResponse? = null
    private lateinit var sauceNaoViewModel: SauceNaoViewModel
    private lateinit var sauceNaoAdapter: SauceNaoAdapter
    private lateinit var openFileObserver: OpenFileLifecycleObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isOrderSuccess) {
            startActivity(Intent(this, PurchaseActivity::class.java))
            finish()
            return
        }
        setContentView(binding.root)
        val list = binding.common.list
        val progressBar = binding.common.progress.progressBar
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.title_sauce_nao)
        }
        sauceNaoAdapter = SauceNaoAdapter()
        list.apply {
            layoutManager = LinearLayoutManager(this@SauceNaoActivity, RecyclerView.VERTICAL, false)
            adapter = sauceNaoAdapter
        }
        sauceNaoViewModel = getSauceNaoViewModel(api)
        sauceNaoViewModel.data.observe(this, {
            response = it
            supportActionBar?.subtitle = String.format(getString(R.string.sauce_nao_remaining_times_today), it.header.longRemaining)
            sauceNaoAdapter.notifyDataSetChanged()
        })
        sauceNaoViewModel.isLoading.observe(this, {
            progressBar.isVisible = it
            if (it && errorMsg.isVisible) {
                errorMsg.isVisible = false
            }
        })
        sauceNaoViewModel.error.observe(this, {
            if (!it.isNullOrBlank()) {
                errorMsg.isVisible = true
                errorMsg.text = it
            } else {
                errorMsg.isVisible = false
            }
        })
        val url = intent?.getStringExtra(SAUCE_NAO_SEARCH_URL_KEY)
        if (!url.isNullOrEmpty()) {
            search(url)
        }
        fab.setOptionsClick(object : OneMoreFabMenu.OptionsClick {
            override fun onOptionClick(optionId: Int?) {
                when (optionId) {
                    R.id.option_url -> searchByUrl()
                    R.id.option_file -> searchByFile()
                }
            }
        })
        openFileObserver = OpenFileLifecycleObserver(activityResultRegistry) { uri ->
            search(uri)
        }
        lifecycle.addObserver(openFileObserver)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.sauce_nao, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sauce_nao_change_api_key -> {
                changeApiKey()
                true
            }
            R.id.action_sauce_nao_get_api_key -> {
                val url = "https://saucenao.com/user.php"
                launchUrl(url)
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun search(url: String) {
        val apiKey = sauceNaoApiKey
        if (apiKey.isNotEmpty()) {
            sauceNaoViewModel.searchByUrl(imageUrl = url, apiKey = apiKey)
        } else {
            errorMsg.toVisibility(true)
            errorMsg.setText(R.string.sauce_nao_api_key_unset)
        }
    }

    private fun searchByUrl() {
        if (isFinishing) {
            return
        }
        val padding = resources.getDimensionPixelSize(R.dimen.spacing_mlarge)
        val layout = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(padding, padding / 2, padding, 0)
        }
        val editText = EditText(this)
        layout.addView(editText)
        AlertDialog.Builder(this)
            .setTitle(R.string.sauce_nao_image_url)
            .setView(layout)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                val url = (editText.text ?: "").toString().trim()
                if (url.startsWith("http")) {
                    search(url)
                } else {
                    Snackbar.make(binding.root, R.string.sauce_nao_invalid_image_url, Snackbar.LENGTH_LONG).show()
                }
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()
            .show()
    }

    private fun searchByFile() {
        openFileObserver.openDocument("image/*")
    }

    override fun onBackPressed() {
        if (fab.isExpanded()) {
            fab.collapse()
        } else {
            super.onBackPressed()
        }
    }

    private fun search(imageUri: Uri) {
        val apiKey = sauceNaoApiKey
        if (apiKey.isNotEmpty()) {
            lifecycleScope.launch {
                val byteArray = withContext(Dispatchers.IO) {
                    try {
                        contentResolver.openInputStream(imageUri)?.readBytes()
                    } catch (_: IOException) {
                        null
                    }
                }
                if (byteArray != null) {
                    sauceNaoViewModel.searchByImage(
                        apiKey = apiKey,
                        byteArray = byteArray,
                        fileExt = imageUri.toDecodedString().fileExt())
                }
            }
        } else {
            errorMsg.isVisible = true
            errorMsg.setText(R.string.sauce_nao_api_key_unset)
        }
    }

    private fun changeApiKey() {
        if (isFinishing) {
            return
        }
        val padding = resources.getDimensionPixelSize(R.dimen.spacing_mlarge)
        val layout = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(padding, padding / 2, padding, 0)
        }
        val editText = EditText(this).apply {
            setText(sauceNaoApiKey)
        }
        layout.addView(editText)
        AlertDialog.Builder(this)
            .setTitle(R.string.sauce_nao_change_api_key)
            .setView(layout)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                val key = (editText.text ?: "").toString().trim()
                sauceNaoApiKey = key
                if (key.isEmpty()) {
                    errorMsg.toVisibility(true)
                    errorMsg.setText(R.string.sauce_nao_api_key_unset)
                } else {
                    errorMsg.toVisibility(false)
                }
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()
            .show()
    }

    inner class SauceNaoAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemCount(): Int = response?.results?.size ?: 0

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int): RecyclerView.ViewHolder = SauceNaoViewHolder(parent)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val result = response?.results?.get(position) ?: return
            (holder as SauceNaoViewHolder).bind(result)
        }

        inner class SauceNaoViewHolder(binding: ItemSauceNaoBinding) : RecyclerView.ViewHolder(binding.root) {

            constructor(parent: ViewGroup): this(parent.viewBinding(ItemSauceNaoBinding::inflate))

            private val thumbnail = binding.thumbnail
            private val title = binding.title
            private val similarity = binding.similarity
            private val info1 = binding.info1
            private val info2 = binding.info2

            private var urls: Array<String>? = null

            init {
                itemView.setOnClickListener {
                    showDialog()
                }
            }

            private fun showDialog() {
                val urls = urls
                if (urls.isNullOrEmpty() || isFinishing) {
                    return
                }
                AlertDialog.Builder(itemView.context)
                    .setTitle(R.string.sauce_nao_source)
                    .setItems(urls) { _, which ->
                        launchUrl(urls[which])
                    }
                    .create()
                    .show()
            }

            fun bind(result: Result) {
                similarity.text = result.header.similarity
                title.text = result.header.indexName
                when {
                    !result.data.characters.isNullOrEmpty() -> {
                        info1.text = String.format("Material: %s", result.data.material ?: "")
                        info2.text = String.format("Characters: %s", result.data.characters)
                    }
                    result.data.pixivId != null -> {
                        info1.text = String.format("Pixiv ID: %d", result.data.pixivId)
                        info2.text = String.format("Title: %s", result.data.title ?: "")
                    }
                    result.data.anidbAid != null -> {
                        info1.text = String.format("Anidb aid: %d", result.data.anidbAid)
                        info2.text = String.format("Source: %s", result.data.source ?: "")
                    }
                    result.data.seigaId != null -> {
                        info1.text = String.format("Seiga ID: %d", result.data.seigaId)
                        info2.text = String.format("Title: %s", result.data.title ?: "")
                    }
                    result.data.daId != null -> {
                        info1.text = String.format("Da ID: %d", result.data.daId)
                        info2.text = String.format("Title: %s", result.data.title ?: "")
                    }
                    result.data.engName != null -> {
                        info1.text = String.format("Eng name: %s", result.data.engName)
                        info2.text = String.format("Jp name: %s", result.data.jpName ?: "")
                    }
                }
                GlideApp.with(itemView.context)
                    .load(result.header.thumbnail)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(thumbnail)
                urls = result.data.extUrls?.toTypedArray()
            }
        }
    }
}
