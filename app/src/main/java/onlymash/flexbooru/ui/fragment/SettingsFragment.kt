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

package onlymash.flexbooru.ui.fragment

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import onlymash.flexbooru.App
import onlymash.flexbooru.R
import onlymash.flexbooru.Settings
import onlymash.flexbooru.util.openDocumentTree

class SettingsFragment : BasePreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.app.sp.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_settings)
        initPathSummary()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            Settings.DOWNLOAD_PATH_KEY -> {
                initPathSummary()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        App.app.sp.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun initPathSummary() {
        var path = Settings.instance().downloadDirPath
        if (path.isNullOrEmpty()) {
            path = getString(R.string.settings_download_path_not_set)
        }
        findPreference<Preference>(Settings.DOWNLOAD_PATH_KEY)?.summary = path
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        if (preference?.key == Settings.DOWNLOAD_PATH_KEY) {
            requireActivity().openDocumentTree()
        }
        return super.onPreferenceTreeClick(preference)
    }
}