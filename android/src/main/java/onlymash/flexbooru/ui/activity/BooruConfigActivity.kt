package onlymash.flexbooru.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import onlymash.flexbooru.R
import onlymash.flexbooru.ui.fragment.BooruConfigFragment
import onlymash.flexbooru.widget.hideNavBar

class BooruConfigActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BOORU_UID = "extra_booru_uid"
    }

    lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booru_config)
        hideNavBar {

        }
        toolbar = findViewById(R.id.toolbar)
        toolbar.apply {
            setTitle(R.string.title_booru_config)
            setNavigationIcon(R.drawable.ic_close_24dp)
            inflateMenu(R.menu.booru_config)
            setNavigationOnClickListener {
                finish()
            }
            setOnMenuItemClickListener(supportFragmentManager.findFragmentById(R.id.fragment_booru_config) as BooruConfigFragment)
        }
    }
}