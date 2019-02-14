package onlymash.flexbooru.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R

class SearchActivity : BaseActivity() {

    companion object {
        fun startActivity(context: Context, keyword: String) {
            context.startActivity(
                Intent(context, SearchActivity::class.java)
                    .putExtra(Constants.KEYWORD_KEY, keyword))
        }
    }

    internal var keyword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        keyword = intent?.extras?.getString(Constants.KEYWORD_KEY) ?: ""
        setContentView(R.layout.activity_search)
    }
}
