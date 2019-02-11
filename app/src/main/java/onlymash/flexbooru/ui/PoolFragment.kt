package onlymash.flexbooru.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.fragment_list.*
import onlymash.flexbooru.R
import onlymash.flexbooru.widget.SearchBar

class PoolFragment : ListFragment() {
    override val helper: SearchBar.Helper
        get() = object : SearchBar.Helper {
            override fun onMenuItemClick(menuItem: MenuItem) {

            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        search_bar.setTitle(R.string.title_pools)
    }
}