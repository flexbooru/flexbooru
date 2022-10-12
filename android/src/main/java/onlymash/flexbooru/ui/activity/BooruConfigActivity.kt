/*
 * Copyright (C) 2020. by onlymash <fiepi.dev@gmail.com>, All rights reserved
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

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import onlymash.flexbooru.R
import onlymash.flexbooru.ui.base.BaseActivity

class BooruConfigActivity : BaseActivity() {

    companion object {
        const val EXTRA_BOORU_UID = "extra_booru_uid"
    }

    private var menuListener: MenuListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booru_config)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.title_booru_config)
        }
        menuListener = supportFragmentManager.findFragmentById(R.id.fragment_booru_config) as? MenuListener
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.booru_config, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            true
        } else {
            menuListener?.onMenuItemClick(item) ?: super.onOptionsItemSelected(item)
        }
    }

    interface MenuListener {
        fun onMenuItemClick(item: MenuItem): Boolean
    }
}