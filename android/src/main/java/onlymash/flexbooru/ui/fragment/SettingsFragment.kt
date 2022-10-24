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

package onlymash.flexbooru.ui.fragment

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Settings.CLEAR_CACHE_KEY
import onlymash.flexbooru.app.Settings.DNS_OVER_HTTPS
import onlymash.flexbooru.app.Settings.DNS_OVER_HTTPS_PROVIDER
import onlymash.flexbooru.app.Settings.DOWNLOAD_PATH_KEY
import onlymash.flexbooru.app.Settings.GRID_MODE_KEY
import onlymash.flexbooru.app.Settings.GRID_RATIO_KEY
import onlymash.flexbooru.app.Settings.NIGHT_MODE_KEY
import onlymash.flexbooru.app.Settings.NIGHT_THEME_KEY
import onlymash.flexbooru.app.Settings.downloadDirPath
import onlymash.flexbooru.app.Settings.gridMode
import onlymash.flexbooru.app.Settings.isDohEnable
import onlymash.flexbooru.app.Settings.nightMode
import onlymash.flexbooru.data.database.dao.PostDao
import onlymash.flexbooru.extension.getTreeUri
import onlymash.flexbooru.extension.toDecodedString
import onlymash.flexbooru.extension.trim
import onlymash.flexbooru.ui.base.PathActivity
import onlymash.flexbooru.ui.helper.isNightEnable
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

class SettingsFragment : PreferenceFragmentCompat(), DIAware, SharedPreferences.OnSharedPreferenceChangeListener {

    override val di by closestDI()
    private val postDao by instance<PostDao>()
    private val sp by instance<SharedPreferences>()

    private var gridRatioPreference: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_settings)
        gridRatioPreference = findPreference(GRID_RATIO_KEY)
        gridRatioPreference?.isVisible = gridMode == "fixed"
        findPreference<Preference>(NIGHT_THEME_KEY)?.isVisible = resources.configuration.isNightEnable()
        downloadDirPath = context?.contentResolver?.getTreeUri()?.toDecodedString()
        initPathSummary()
        setupDnsPreference()
        sp.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            NIGHT_THEME_KEY -> {
                if (resources.configuration.isNightEnable()) {
                    activity?.recreate()
                }
            }
            DOWNLOAD_PATH_KEY -> initPathSummary()
            NIGHT_MODE_KEY -> AppCompatDelegate.setDefaultNightMode(nightMode)
            GRID_MODE_KEY -> gridRatioPreference?.isVisible = gridMode == "fixed"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sp.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun setupDnsPreference(changed: Boolean = false) {
        findPreference<ListPreference>(DNS_OVER_HTTPS_PROVIDER)?.isVisible = isDohEnable
        if (changed) {
            findPreference<SwitchPreferenceCompat>(DNS_OVER_HTTPS)?.apply {
                val tipSummary = getString(R.string.settings_dns_over_https_summary)
                summaryOff = tipSummary
                summaryOn = tipSummary
            }
        }
    }

    private fun initPathSummary() {
        var path = downloadDirPath
        if (path.isNullOrEmpty()) {
            path = getString(R.string.settings_download_path_not_set)
        }
        findPreference<Preference>(DOWNLOAD_PATH_KEY)?.summary = path
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            DOWNLOAD_PATH_KEY -> (activity as? PathActivity)?.pickDir()
            CLEAR_CACHE_KEY -> createClearDialog()
            DNS_OVER_HTTPS,
            DNS_OVER_HTTPS_PROVIDER -> setupDnsPreference(true)
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun createClearDialog() {
        val activity = activity
        if (activity == null || activity.isFinishing) {
            return
        }
        AlertDialog.Builder(activity)
            .setTitle(R.string.settings_clear_cache)
            .setMessage(R.string.settings_clear_cache_dialog_content)
            .setNegativeButton(R.string.dialog_cancel, null)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                trimCache()
            }
            .show()
    }

    private fun trimCache() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                postDao.deleteAll()
            } catch (_: Exception) {}
            context?.cacheDir?.trim()
        }
    }
}