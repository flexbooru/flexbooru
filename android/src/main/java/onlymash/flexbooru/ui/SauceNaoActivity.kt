package onlymash.flexbooru.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_sauce_nao.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.Dispatchers
import onlymash.flexbooru.R
import onlymash.flexbooru.extension.toVisibility
import onlymash.flexbooru.saucenao.model.SauceNaoResponse
import onlymash.flexbooru.saucenao.presentation.SauceNaoActions
import onlymash.flexbooru.saucenao.presentation.SauceNaoPresenter
import onlymash.flexbooru.saucenao.presentation.SauceNaoView
import kotlin.properties.Delegates

private const val TAG = "SauceNaoActivity"

class SauceNaoActivity : AppCompatActivity(), SauceNaoView {

    @Suppress("EXPERIMENTAL_API_USAGE")
    private val actions: SauceNaoActions by lazy {
        SauceNaoPresenter(Dispatchers.Main, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sauce_nao)
        toolbar.setTitle(R.string.title_sauce_nao)
        val url = ""
        val apiKey = ""
        actions.onRequestData(imageUrl = url, apiKey = apiKey)
    }

    override var isUpdating: Boolean by Delegates.observable(false) {_, _, isLoading ->
        progress_bar.toVisibility(isLoading)
    }

    override fun onUpdate(data: SauceNaoResponse) {
        Log.w(TAG, data.toString())
    }

    override fun showError(error: Throwable) {
        Log.w(TAG, error.toString())
    }
}
