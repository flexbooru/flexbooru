/*
 * Copyright (C) 2019 by onlymash <im@fiepi.me>, All rights reserved
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

package onlymash.flexbooru.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.App
import onlymash.flexbooru.R
import onlymash.flexbooru.Settings

class SettingsActivity : BaseActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        toolbar.setTitle(R.string.title_settings)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        App.app.sp.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            Settings.THEME_MODE_KEY -> {
                val mode = Settings.instance().themeMode
                AppCompatDelegate.setDefaultNightMode(mode)
                if (mode != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
                    recreate()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        App.app.sp.unregisterOnSharedPreferenceChangeListener(this)
    }
}
