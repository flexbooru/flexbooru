package onlymash.flexbooru.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Keys.POST_QUERY

class SearchActivity : AppCompatActivity() {

    companion object {
        fun startSearch(context: Context, query: String) {
            context.startActivity(Intent(context, SearchActivity::class.java)
                .putExtra(POST_QUERY, query))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
    }
}