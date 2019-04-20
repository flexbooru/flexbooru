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

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.appcompat.app.AppCompatDelegate
import moe.shizuku.preference.Preference
import onlymash.flexbooru.*
import onlymash.flexbooru.util.getBasePath
import java.net.URLDecoder

class SettingsFragment : BasePreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.app.sp.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_settings)
        initPathSummary()
    }
    override fun onCreateItemDecoration(): DividerDecoration? {
        return CategoryDivideDividerDecoration()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            Settings.THEME_MODE_KEY -> {
                val mode = Settings.instance().themeMode
                AppCompatDelegate.setDefaultNightMode(mode)
                if (mode != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
                    requireActivity().recreate()
                }
            }
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
            path = getBasePath()
            Settings.instance().downloadDirPath = path
        }
        findPreference(Settings.DOWNLOAD_PATH_KEY)?.summary = path
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        if (preference?.key == Settings.DOWNLOAD_PATH_KEY) {
            try {
                startActivityForResult(
                    Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                                or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                                or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                    },
                    Constants.REQUEST_CODE_OPEN_DIRECTORY)
            } catch (_: ActivityNotFoundException) {}
        }
        return super.onPreferenceTreeClick(preference)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CODE_OPEN_DIRECTORY && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            val docUri = DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri)) ?: return
            Settings.instance().downloadDirPath = URLDecoder.decode(docUri.toString(), "UTF-8")
        }
    }
}