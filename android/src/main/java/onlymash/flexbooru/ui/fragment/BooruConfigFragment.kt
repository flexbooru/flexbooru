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
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL_LEGACY
import onlymash.flexbooru.app.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.app.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.app.Values.BOORU_TYPE_SHIMMIE
import onlymash.flexbooru.app.Values.HASH_SALT_CONTAINED
import onlymash.flexbooru.app.Values.SCHEME_HTTPS
import onlymash.flexbooru.data.database.dao.BooruDao
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.extension.isHost
import onlymash.flexbooru.ui.activity.BooruConfigActivity
import org.koin.android.ext.android.inject

private const val CONFIG_NAME_KEY = "booru_config_name"
private const val CONFIG_TYPE_KEY = "booru_config_type"
private const val CONFIG_SCHEME_KEY = "booru_config_scheme"
private const val CONFIG_HOST_KEY = "booru_config_host"
private const val CONFIG_PATH_KEY = "booru_config_path"
private const val CONFIG_HASH_SALT_KEY = "booru_config_hash_salt"

private const val CONFIG_TYPE_DAN = "danbooru"
private const val CONFIG_TYPE_DAN1 = "danbooru1"
private const val CONFIG_TYPE_MOE = "moebooru"
private const val CONFIG_TYPE_GEL = "gelbooru"
private const val CONFIG_TYPE_GEL_LEGACY = "gelbooru_legacy"
private const val CONFIG_TYPE_SANKAKU = "sankaku"
private const val CONFIG_TYPE_SHIMMIE = "shimmie"

class BooruConfigFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener, BooruConfigActivity.MenuListener {

    private val booruDao by inject<BooruDao>()
    private val sp by inject<SharedPreferences>()

    private var booru: Booru? = null

    private var hashSaltPreferences: Preference? = null
    private var pathPreferences: Preference? = null

    private var name: String
        get() = sp.getString(CONFIG_NAME_KEY, "") ?: ""
        set(value) = sp.edit().putString(CONFIG_NAME_KEY, value).apply()

    private var scheme: String
        get() = sp.getString(CONFIG_SCHEME_KEY, SCHEME_HTTPS) ?: SCHEME_HTTPS
        set(value) = sp.edit().putString(CONFIG_SCHEME_KEY, value).apply()

    private var host: String
        get() = sp.getString(CONFIG_HOST_KEY, "") ?: ""
        set(value) = sp.edit().putString(CONFIG_HOST_KEY, value).apply()

    private var path: String?
        get() = sp.getString(CONFIG_PATH_KEY, "")
        set(value) = sp.edit().putString(CONFIG_PATH_KEY, value).apply()

    private var type: Int
        get() = getBooruTypeInt(sp.getString(CONFIG_TYPE_KEY, CONFIG_TYPE_MOE) ?: CONFIG_TYPE_MOE)
        set(value) = sp.edit().putString(CONFIG_TYPE_KEY, getBooruTypeString(value)).apply()

    private var hashSalt: String
        get() = sp.getString(CONFIG_HASH_SALT_KEY, "") ?: ""
        set(value) = sp.edit().putString(CONFIG_HASH_SALT_KEY, value).apply()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val activity = activity as BooruConfigActivity
        val booruUid = activity.intent?.getLongExtra(BooruConfigActivity.EXTRA_BOORU_UID, -1L) ?: -1L
        if (booruUid >= 0) {
            booru = booruDao.getBooruByUid(booruUid)
        }
        if (booru == null) {
            booru = Booru(
                name = "",
                scheme = SCHEME_HTTPS,
                host = "",
                type = BOORU_TYPE_MOE,
                hashSalt = "--$HASH_SALT_CONTAINED--"
            )
        }
        booru?.let {
            name = it.name
            scheme = it.scheme
            host = it.host
            type = it.type
            hashSalt = it.hashSalt
            path = it.path
        }
        addPreferencesFromResource(R.xml.pref_booru_config)
        findPreference<EditTextPreference>(CONFIG_HOST_KEY)?.setOnBindEditTextListener {
            it.hint = "example.com"
        }
        hashSaltPreferences = findPreference(CONFIG_HASH_SALT_KEY)
        hashSaltPreferences?.isVisible = type == BOORU_TYPE_DAN1
        pathPreferences = findPreference(CONFIG_PATH_KEY)
        pathPreferences?.isVisible = type == BOORU_TYPE_SHIMMIE
        sp.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        sp.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val booru = booru ?: return true
        when (item.itemId) {
            R.id.action_booru_config_delete -> {
                context?.let { context ->
                    AlertDialog.Builder(context)
                        .setTitle(R.string.booru_config_dialog_title_delete)
                        .setPositiveButton(R.string.dialog_yes) { _, _ ->
                            booruDao.delete(booru.uid)
                            activity?.finish()
                        }
                        .setNegativeButton(R.string.dialog_no, null)
                        .create()
                        .show()
                }
            }
            R.id.action_booru_config_apply -> {
                when {
                    booru.name.isEmpty() -> {
                        Snackbar.make(listView, R.string.booru_config_name_cant_empty, Snackbar.LENGTH_LONG).show()
                    }
                    booru.host.isEmpty() -> {
                        Snackbar.make(listView, R.string.booru_config_host_cant_empty, Snackbar.LENGTH_LONG).show()
                    }
                    !booru.host.isHost() -> {
                        Snackbar.make(listView, getString(R.string.booru_config_host_invalid), Snackbar.LENGTH_LONG).show()
                    }
                    booru.type == BOORU_TYPE_DAN1 && booru.hashSalt.isEmpty() -> {
                        Snackbar.make(listView, R.string.booru_config_hash_salt_cant_empty, Snackbar.LENGTH_LONG).show()
                    }
                    booru.type == BOORU_TYPE_DAN1 && !booru.hashSalt.contains(HASH_SALT_CONTAINED) -> {
                        Snackbar.make(listView, getString(R.string.booru_config_hash_salt_must_contain_yp), Snackbar.LENGTH_LONG).show()
                    }
                    else -> {
                        if (booru.type != BOORU_TYPE_DAN1) {
                            booru.hashSalt = ""
                        }
                        if (booru.type != BOORU_TYPE_SHIMMIE) {
                            booru.path = null
                        }
                        if (booru.uid == 0L) {
                            booruDao.insert(booru)
                        } else {
                            booruDao.update(booru)
                        }
                        activity?.finish()
                    }
                }
            }
        }
        return true
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when(key) {
            CONFIG_NAME_KEY -> booru?.name = name
            CONFIG_SCHEME_KEY -> booru?.scheme = scheme
            CONFIG_HOST_KEY -> booru?.host = host
            CONFIG_TYPE_KEY -> {
                val type = type
                booru?.type = type
                hashSaltPreferences?.isVisible = type == BOORU_TYPE_DAN1
                pathPreferences?.isVisible = type == BOORU_TYPE_SHIMMIE
            }
            CONFIG_HASH_SALT_KEY -> booru?.hashSalt = hashSalt
            CONFIG_PATH_KEY -> booru?.path = path
        }
    }

    private fun getBooruTypeString(booruType: Int): String {
        return when (booruType) {
            BOORU_TYPE_MOE -> CONFIG_TYPE_MOE
            BOORU_TYPE_DAN -> CONFIG_TYPE_DAN
            BOORU_TYPE_DAN1 -> CONFIG_TYPE_DAN1
            BOORU_TYPE_GEL -> CONFIG_TYPE_GEL
            BOORU_TYPE_GEL_LEGACY -> CONFIG_TYPE_GEL_LEGACY
            BOORU_TYPE_SHIMMIE -> CONFIG_TYPE_SHIMMIE
            else -> CONFIG_TYPE_SANKAKU
        }
    }

    private fun getBooruTypeInt(booruType: String): Int {
        return when (booruType) {
            CONFIG_TYPE_MOE -> BOORU_TYPE_MOE
            CONFIG_TYPE_DAN -> BOORU_TYPE_DAN
            CONFIG_TYPE_DAN1 -> BOORU_TYPE_DAN1
            CONFIG_TYPE_GEL -> BOORU_TYPE_GEL
            CONFIG_TYPE_GEL_LEGACY -> BOORU_TYPE_GEL_LEGACY
            CONFIG_TYPE_SHIMMIE -> BOORU_TYPE_SHIMMIE
            else -> BOORU_TYPE_SANKAKU
        }
    }
}