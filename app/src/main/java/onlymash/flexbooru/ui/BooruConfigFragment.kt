package onlymash.flexbooru.ui

import android.content.SharedPreferences
import android.os.Bundle
import moe.shizuku.preference.Preference
import moe.shizuku.preference.PreferenceFragment
import onlymash.flexbooru.App.Companion.app
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.entity.Booru

class BooruConfigFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        var booruUid = -1L
        fun reset() {
            booruUid = -1L
            app.sp.edit().apply {
                putString(Constants.BOORU_CONFIG_NAME_KEY, "")
                putString(Constants.BOORU_CONFIG_TYPE_KEY, Constants.BOORU_CONFIG_TYPE_DANBOORU)
                putString(Constants.BOORU_CONFIG_SCHEME_KEY, Constants.BOORU_CONFIG_SCHEME_HTTPS)
                putString(Constants.BOORU_CONFIG_HOST_KEY, "")
                putString(Constants.BOORU_CONFIG_HASH_SALT_KEY, "")
            }.apply()
        }
        fun set(booru: Booru) {
            var hashSalt = ""
            val type = when (booru.type) {
                Constants.TYPE_DANBOORU -> Constants.BOORU_CONFIG_TYPE_DANBOORU
                Constants.TYPE_MOEBOORU -> {
                    if (booru.hash_salt.isNotBlank()) {
                        hashSalt = booru.hash_salt
                    }
                    Constants.BOORU_CONFIG_TYPE_MOEBOORU
                }
                else -> throw IllegalArgumentException("unknown booru type: ${booru.type}")
            }
            app.sp.edit().apply {
                putString(Constants.BOORU_CONFIG_NAME_KEY, booru.name)
                putString(Constants.BOORU_CONFIG_TYPE_KEY, type)
                putString(Constants.BOORU_CONFIG_SCHEME_KEY, booru.scheme)
                putString(Constants.BOORU_CONFIG_HOST_KEY, booru.host)
                putString(Constants.BOORU_CONFIG_HASH_SALT_KEY, hashSalt)
            }.apply()
        }
        fun get(): Booru {
            return Booru(
                uid = booruUid,
                name = getName(app.sp),
                scheme = getScheme(app.sp),
                host = getHost(app.sp),
                hash_salt = getHashSalt(app.sp),
                type = getTypeInt(app.sp)
            )
        }
        private fun getTypeInt(sp: SharedPreferences): Int {
            return when (sp.getString(Constants.BOORU_CONFIG_TYPE_KEY, Constants.BOORU_CONFIG_TYPE_DANBOORU)) {
                Constants.BOORU_CONFIG_TYPE_MOEBOORU -> Constants.TYPE_MOEBOORU
                else -> Constants.TYPE_DANBOORU
            }
        }
        private fun getScheme(sp: SharedPreferences): String {
            return when (sp.getString(Constants.BOORU_CONFIG_SCHEME_KEY, Constants.BOORU_CONFIG_SCHEME_HTTPS)) {
                Constants.BOORU_CONFIG_SCHEME_HTTPS -> Constants.BOORU_CONFIG_SCHEME_HTTPS
                else -> Constants.BOORU_CONFIG_SCHEME_HTTP
            }
        }
        private fun getName(sp: SharedPreferences): String {
            val name = sp.getString(Constants.BOORU_CONFIG_NAME_KEY, "")
            return if (name.isNullOrEmpty()) "" else name
        }
        private fun getHost(sp: SharedPreferences): String {
            val host = sp.getString(Constants.BOORU_CONFIG_HOST_KEY, "")
            return if (host.isNullOrEmpty()) "" else host
        }
        private fun getHashSalt(sp: SharedPreferences): String {
            if (getTypeInt(sp) == Constants.TYPE_DANBOORU) return ""
            val hashSalt = sp.getString(Constants.BOORU_CONFIG_HASH_SALT_KEY, "")
            return if (hashSalt.isNullOrEmpty()) "" else hashSalt
        }
    }

    private lateinit var hashSaltPreferences: Preference
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        booruUid = requireActivity().intent.getLongExtra(Constants.EXTRA_BOORU_UID, -1L)
        addPreferencesFromResource(R.xml.pref_booru_config)
        hashSaltPreferences = findPreference(Constants.BOORU_CONFIG_HASH_SALT_KEY)
        if (booruUid < 0 ||
            app.sp.getString(Constants.BOORU_CONFIG_TYPE_KEY, Constants.BOORU_CONFIG_TYPE_DANBOORU)
            == Constants.BOORU_CONFIG_TYPE_DANBOORU) {
            hashSaltPreferences.isVisible = false
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == Constants.BOORU_CONFIG_TYPE_KEY) {
            when (getType(sharedPreferences)) {
                Constants.BOORU_CONFIG_TYPE_DANBOORU -> {
                    hashSaltPreferences.isVisible = false
                }
                Constants.BOORU_CONFIG_TYPE_MOEBOORU -> {
                    hashSaltPreferences.isVisible = true
                }
            }
        }
    }

    override fun onCreateItemDecoration(): DividerDecoration? {
        return CategoryDivideDividerDecoration()
    }

    override fun onPause() {
        super.onPause()
        app.sp.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        app.sp.registerOnSharedPreferenceChangeListener(this)
    }

    private fun getType(sp: SharedPreferences): String {
        return when (sp.getString(Constants.BOORU_CONFIG_TYPE_KEY, Constants.BOORU_CONFIG_TYPE_DANBOORU)) {
            Constants.BOORU_CONFIG_TYPE_MOEBOORU -> Constants.BOORU_CONFIG_TYPE_MOEBOORU
            else -> Constants.BOORU_CONFIG_TYPE_DANBOORU
        }
    }
}