package onlymash.flexbooru.ui

import android.os.Bundle
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.R

class BooruActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booru)
        initToolbar()
    }
    private fun initToolbar(){
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.inflateMenu(R.menu.menu_booru)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_booru_add -> {

                }
                R.id.action_booru_add_qr -> {

                }
                R.id.action_booru_add_clipboard -> {

                }
                R.id.action_booru_add_manual -> {

                }
                else -> {
                    // unknown id
                }
            }
            true
        }
    }
}
