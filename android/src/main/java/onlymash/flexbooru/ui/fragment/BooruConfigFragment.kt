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
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import onlymash.flexbooru.common.App
import onlymash.flexbooru.common.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.entity.common.Booru
import org.kodein.di.erased.instance

class BooruConfigFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        const val EXTRA_BOORU_UID = "booru_uid"
        const val BOORU_CONFIG_NAME_KEY = "booru_config_name"
        const val BOORU_CONFIG_TYPE_KEY = "booru_config_type"
        const val BOORU_CONFIG_TYPE_DANBOORU = "danbooru"
        const val BOORU_CONFIG_TYPE_DANBOORU_ONE = "danbooru_one"
        const val BOORU_CONFIG_TYPE_MOEBOORU = "moebooru"
        const val BOORU_CONFIG_TYPE_GELBOORU = "gelbooru"
        const val BOORU_CONFIG_TYPE_SANKAKU = "sankaku"
        const val BOORU_CONFIG_SCHEME_KEY = "booru_config_scheme"
        const val BOORU_CONFIG_SCHEME_HTTP = "http"
        const val BOORU_CONFIG_SCHEME_HTTPS = "https"
        const val BOORU_CONFIG_HOST_KEY = "booru_config_host"
        const val BOORU_CONFIG_hashSalt_KEY = "booru_config_hashSalt"
        private val sp: SharedPreferences by App.app.instance()
        var booruUid = -1L
        fun reset() {
            booruUid = -1L
            sp.edit().apply {
                putString(BOORU_CONFIG_NAME_KEY, "")
                putString(BOORU_CONFIG_TYPE_KEY, BOORU_CONFIG_TYPE_DANBOORU)
                putString(BOORU_CONFIG_SCHEME_KEY, BOORU_CONFIG_SCHEME_HTTPS)
                putString(BOORU_CONFIG_HOST_KEY, "")
                putString(BOORU_CONFIG_hashSalt_KEY, "")
            }.apply()
        }
        fun set(booru: Booru) {
            var hashSalt = ""
            val type = when (booru.type) {
                Constants.TYPE_DANBOORU -> BOORU_CONFIG_TYPE_DANBOORU
                Constants.TYPE_MOEBOORU -> {
                    if (booru.hashSalt.isNotBlank()) {
                        hashSalt = booru.hashSalt
                    }
                    BOORU_CONFIG_TYPE_MOEBOORU
                }
                Constants.TYPE_DANBOORU_ONE -> {
                    if (booru.hashSalt.isNotBlank()) {
                        hashSalt = booru.hashSalt
                    }
                    BOORU_CONFIG_TYPE_DANBOORU_ONE
                }
                Constants.TYPE_GELBOORU -> BOORU_CONFIG_TYPE_GELBOORU
                Constants.TYPE_SANKAKU -> {
                    if (booru.hashSalt.isNotBlank()) {
                        hashSalt = booru.hashSalt
                    }
                    BOORU_CONFIG_TYPE_SANKAKU
                }
                else -> throw IllegalArgumentException("unknown booru type: ${booru.type}")
            }
            sp.edit().apply {
                putString(BOORU_CONFIG_NAME_KEY, booru.name)
                putString(BOORU_CONFIG_TYPE_KEY, type)
                putString(BOORU_CONFIG_SCHEME_KEY, booru.scheme)
                putString(BOORU_CONFIG_HOST_KEY, booru.host)
                putString(BOORU_CONFIG_hashSalt_KEY, hashSalt)
            }.apply()
        }
        fun get(): Booru {
            return Booru(
                uid = booruUid,
                name = getName(sp),
                scheme = getScheme(sp),
                host = getHost(sp),
                hashSalt = getHashSalt(sp),
                type = getTypeInt(sp)
            )
        }
        private fun getTypeInt(sp: SharedPreferences): Int {
            return when (sp.getString(BOORU_CONFIG_TYPE_KEY, BOORU_CONFIG_TYPE_DANBOORU)) {
                BOORU_CONFIG_TYPE_DANBOORU -> Constants.TYPE_DANBOORU
                BOORU_CONFIG_TYPE_MOEBOORU -> Constants.TYPE_MOEBOORU
                BOORU_CONFIG_TYPE_DANBOORU_ONE -> Constants.TYPE_DANBOORU_ONE
                BOORU_CONFIG_TYPE_GELBOORU -> Constants.TYPE_GELBOORU
                else -> Constants.TYPE_SANKAKU
            }
        }
        private fun getScheme(sp: SharedPreferences): String {
            return when (sp.getString(BOORU_CONFIG_SCHEME_KEY, BOORU_CONFIG_SCHEME_HTTPS)) {
                BOORU_CONFIG_SCHEME_HTTPS -> BOORU_CONFIG_SCHEME_HTTPS
                else -> BOORU_CONFIG_SCHEME_HTTP
            }
        }
        private fun getName(sp: SharedPreferences): String {
            val name = sp.getString(BOORU_CONFIG_NAME_KEY, "")
            return if (name.isNullOrEmpty()) "" else name
        }
        private fun getHost(sp: SharedPreferences): String {
            val host = sp.getString(BOORU_CONFIG_HOST_KEY, "")
            return if (host.isNullOrEmpty()) "" else host
        }
        private fun getHashSalt(sp: SharedPreferences): String {
            when (getTypeInt(sp)) {
                Constants.TYPE_DANBOORU,
                Constants.TYPE_GELBOORU -> return ""
            }
            val hashSalt = sp.getString(BOORU_CONFIG_hashSalt_KEY, "")
            return if (hashSalt.isNullOrEmpty()) "" else hashSalt
        }
    }

    private var hashSaltPreferences: Preference? = null
    
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        booruUid = requireActivity().intent.getLongExtra(EXTRA_BOORU_UID, -1L)
        addPreferencesFromResource(R.xml.pref_booru_config)
        hashSaltPreferences = findPreference(BOORU_CONFIG_hashSalt_KEY)
        when (sp.getString(BOORU_CONFIG_TYPE_KEY, BOORU_CONFIG_TYPE_DANBOORU)) {
            BOORU_CONFIG_TYPE_DANBOORU,
            BOORU_CONFIG_TYPE_GELBOORU -> {
                hashSaltPreferences?.isVisible = false
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == BOORU_CONFIG_TYPE_KEY) {
            when (sharedPreferences.getString(BOORU_CONFIG_TYPE_KEY, BOORU_CONFIG_TYPE_DANBOORU)) {
                BOORU_CONFIG_TYPE_DANBOORU,
                BOORU_CONFIG_TYPE_GELBOORU -> {
                    hashSaltPreferences?.isVisible = false
                }
                BOORU_CONFIG_TYPE_MOEBOORU,
                BOORU_CONFIG_TYPE_DANBOORU_ONE,
                BOORU_CONFIG_TYPE_SANKAKU -> {
                    hashSaltPreferences?.isVisible = true
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sp.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        sp.registerOnSharedPreferenceChangeListener(this)
    }
}