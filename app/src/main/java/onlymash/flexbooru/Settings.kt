package onlymash.flexbooru

import android.content.SharedPreferences
import onlymash.flexbooru.App.Companion.app

class Settings(private val sp: SharedPreferences) {
    companion object {
        private var instance: Settings? = null
        fun instance(): Settings {
            if (instance == null) {
                instance = Settings(app.sp)
            }
            return instance!!
        }
        const val SAFE_MODE_KEY = "settings_safe_mode"
        const val POST_LIMIT_KEY = "settings_post_limit"
    }

    var activeBooruUid: Long
        get() = sp.getLong(Constants.ACTIVE_BOORU_UID_KEY, -1)
        set(value) = sp.edit().putLong(Constants.ACTIVE_BOORU_UID_KEY, value).apply()

    var safeMode: Boolean
        get() = sp.getBoolean(SAFE_MODE_KEY, true)
        set(value) = sp.edit().putBoolean(SAFE_MODE_KEY, value).apply()

    var postLimit: Int
        get() = sp.getString(POST_LIMIT_KEY, "10")!!.toInt()
        set(value) = sp.edit().putString(POST_LIMIT_KEY, value.toString()).apply()
}