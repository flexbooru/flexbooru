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

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import onlymash.flexbooru.R
import onlymash.flexbooru.ui.fragment.BooruConfigFragment
import onlymash.flexbooru.widget.drawNavBar

class BooruConfigActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BOORU_UID = "extra_booru_uid"
    }

    lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booru_config)
        drawNavBar {

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