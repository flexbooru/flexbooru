package onlymash.flexbooru.ui.activity

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dekoservidoni.omfm.OneMoreFabMenu
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_sauce_nao.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.errors.IOException
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.extension.launchUrl
import onlymash.flexbooru.extension.toVisibility
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.saucenao.model.Result
import onlymash.flexbooru.saucenao.model.SauceNaoResponse
import onlymash.flexbooru.saucenao.presentation.SauceNaoActions
import onlymash.flexbooru.saucenao.presentation.SauceNaoPresenter
import onlymash.flexbooru.saucenao.presentation.SauceNaoView
import java.io.ByteArrayOutputStream
import kotlin.properties.Delegates

const val SAUCE_NAO_SEARCH_URL_KEY = "sauce_nao_search_url"

private const val READ_IMAGE_REQUEST_CODE = 147

class SauceNaoActivity : AppCompatActivity(), SauceNaoView {

    companion object {
        fun startSearch(context: Context, url: String) {
            context.startActivity(
                Intent(context, SauceNaoActivity::class.java).apply {
                    putExtra(SAUCE_NAO_SEARCH_URL_KEY, url)
                }
            )
        }
    }

    private var response: SauceNaoResponse? = null

    private lateinit var sauceNaoAdapter: SauceNaoAdapter

    @Suppress("EXPERIMENTAL_API_USAGE")
    private val actions: SauceNaoActions by lazy {
        SauceNaoPresenter(Dispatchers.Main, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sauce_nao)
        toolbar.setTitle(R.string.title_sauce_nao)
        toolbar.inflateMenu(R.menu.sauce_nao)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_sauce_nao_change_api_key -> {
                    changeApiKey()
                }
                R.id.action_sauce_nao_get_api_key -> {
                    val url = "https://saucenao.com/user.php"
                    launchUrl(url)
                }
            }
            true
        }
        sauceNaoAdapter = SauceNaoAdapter()
        sauce_nao_list.apply {
            layoutManager = LinearLayoutManager(this@SauceNaoActivity, RecyclerView.VERTICAL, false)
            adapter = sauceNaoAdapter
        }
        if (!Settings.isOrderSuccess) {
            startActivity(Intent(this, PurchaseActivity::class.java))
            finish()
            return
        }
        val url = intent?.getStringExtra(SAUCE_NAO_SEARCH_URL_KEY)
        if (!url.isNullOrEmpty()) {
            search(url)
        }
        sauce_nao_search_fab.setOptionsClick(object : OneMoreFabMenu.OptionsClick {
            override fun onOptionClick(optionId: Int?) {
                when (optionId) {
                    R.id.option_url -> searchByUrl()
                    R.id.option_file -> searchByFile()
                }
            }
        })
    }

    private fun search(url: String) {
        val apiKey = Settings.sauceNaoApiKey
        if (apiKey.isNotEmpty()) {
            actions.onRequestData(apiKey = apiKey, imageUrl = url)
        } else {
            error_msg.toVisibility(true)
            error_msg.setText(R.string.sauce_nao_api_key_unset)
        }
    }

    private fun searchByUrl() {
        val padding = resources.getDimensionPixelSize(R.dimen.spacing_mlarge)
        val layout = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(padding, padding, padding, 0)
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
                    Snackbar.make(root_container, R.string.sauce_nao_invalid_image_url, Snackbar.LENGTH_LONG).show()
                }
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()
            .show()
    }

    private fun searchByFile() {
        try {
            startActivityForResult(
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "image/*"
                },
                READ_IMAGE_REQUEST_CODE)
        } catch (_: ActivityNotFoundException) {}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == READ_IMAGE_REQUEST_CODE) {
            data?.data?.also {
                search(it)
            }
        }
    }

    private fun search(imageUri: Uri) {
        val apiKey = Settings.sauceNaoApiKey
        if (apiKey.isNotEmpty()) {
            progress_bar.toVisibility(true)
            lifecycleScope.launch {
                val byteArray = withContext(Dispatchers.IO) {
                    try {
                        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, imageUri))
                        } else {
                            @Suppress("DEPRECATION")
                            MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                        }
                        val os = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                        os.toByteArray()
                    } catch (_: IOException) {
                        null
                    }
                }
                if (byteArray != null) {
                    actions.onRequestData(apiKey = apiKey, byteArray = byteArray)
                } else {
                    progress_bar.toVisibility(false)
                }
            }
        } else {
            error_msg.toVisibility(true)
            error_msg.setText(R.string.sauce_nao_api_key_unset)
        }
    }

    private fun changeApiKey() {
        val padding = resources.getDimensionPixelSize(R.dimen.spacing_mlarge)
        val layout = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(padding, padding, padding, 0)
        }
        val editText = EditText(this).apply {
            setText(Settings.sauceNaoApiKey)
        }
        layout.addView(editText)
        AlertDialog.Builder(this)
            .setTitle(R.string.sauce_nao_change_api_key)
            .setView(layout)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                val key = (editText.text ?: "").toString().trim()
                Settings.sauceNaoApiKey = key
                if (key.isEmpty()) {
                    error_msg.toVisibility(true)
                    error_msg.setText(R.string.sauce_nao_api_key_unset)
                } else {
                    error_msg.toVisibility(false)
                }
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()
            .show()
    }

    override var isUpdating: Boolean by Delegates.observable(false) {_, _, isLoading ->
        progress_bar.toVisibility(isLoading)
        if (isLoading) {
            error_msg.toVisibility(false)
        }
    }

    override fun onUpdate(data: SauceNaoResponse) {
        response = data
        toolbar.subtitle = getString(R.string.sauce_nao_remaining_times_today, data.header.longRemaining)
        sauceNaoAdapter.notifyDataSetChanged()
    }

    override fun showError(error: Throwable) {
        error_msg.toVisibility(true)
        error_msg.text = error.message ?: ""
    }

    inner class SauceNaoAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemCount(): Int = response?.results?.size ?: 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            SauceNaoViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_sauce_nao, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val result = response?.results?.get(position) ?: return
            (holder as SauceNaoViewHolder).bind(result)
        }

        inner class SauceNaoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val thumbnail: AppCompatImageView = itemView.findViewById(R.id.thumbnail)
            private val title: AppCompatTextView = itemView.findViewById(R.id.title)
            private val similarity: AppCompatTextView = itemView.findViewById(R.id.similarity)
            private val info1: AppCompatTextView = itemView.findViewById(R.id.info_1)
            private val info2: AppCompatTextView = itemView.findViewById(R.id.info_2)

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
                    .into(thumbnail)
                val urls = result.data.extUrls?.toTypedArray()
                if (!urls.isNullOrEmpty()) {
                    itemView.setOnClickListener {
                        AlertDialog.Builder(itemView.context)
                            .setTitle(R.string.sauce_nao_source)
                            .setItems(urls) { _, which ->
                                launchUrl(urls[which])
                            }
                            .create()
                            .show()
                    }
                }
            }
        }
    }
}
