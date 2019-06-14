/*
 * Copyright (C) 2019. by onlymash <im@fiepi.me>, All rights reserved
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
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.database.SuggestionManager
import onlymash.flexbooru.extension.openDocumentTree
import onlymash.flexbooru.extension.trimCache
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.erased.instance

class SettingsFragment : PreferenceFragmentCompat(), KodeinAware, SharedPreferences.OnSharedPreferenceChangeListener {

    override val kodein: Kodein by kodein()

    private val sp: SharedPreferences by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sp.registerOnSharedPreferenceChangeListener(this)
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
        sp.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun initPathSummary() {
        var path = Settings.downloadDirPath
        if (path.isNullOrEmpty()) {
            path = getString(R.string.settings_download_path_not_set)
        }
        findPreference<Preference>(Settings.DOWNLOAD_PATH_KEY)?.summary = path
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            Settings.DOWNLOAD_PATH_KEY -> {
                requireActivity().openDocumentTree()
            }
            Settings.CLEAR_CACHE_KEY -> {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.settings_clear_cache)
                    .setMessage(R.string.settings_clear_cache_dialog_content)
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .setPositiveButton(R.string.dialog_ok) { _, _ ->
                        context?.let {
                            lifecycleScope.launch(Dispatchers.IO) {
                                it.trimCache()
                            }
                        }
                    }
                    .show()
            }
            Settings.CLEAR_HISTORY_KEY -> {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.settings_clear_history)
                    .setMessage(R.string.settings_clear_history_dialog_content)
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .setPositiveButton(R.string.dialog_ok) { _, _ ->
                        SuggestionManager.deleteAll()
                    }
                    .show()
            }
        }
        return super.onPreferenceTreeClick(preference)
    }
}