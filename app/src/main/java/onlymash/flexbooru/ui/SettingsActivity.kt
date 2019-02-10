package onlymash.flexbooru.ui

import android.os.Bundle
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.R

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        toolbar.setTitle(R.string.title_settings)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}
