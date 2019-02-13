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
        const val PAGE_SIZE_KEY = "settings_page_size"
    }

    var activeBooruUid: Long
        get() = sp.getLong(Constants.ACTIVE_BOORU_UID_KEY, -1)
        set(value) = sp.edit().putLong(Constants.ACTIVE_BOORU_UID_KEY, value).apply()

    var safeMode: Boolean
        get() = sp.getBoolean(SAFE_MODE_KEY, true)
        set(value) = sp.edit().putBoolean(SAFE_MODE_KEY, value).apply()

    var pageSize: Int
        get() = sp.getString(PAGE_SIZE_KEY, "10")!!.toInt()
        set(value) = sp.edit().putString(PAGE_SIZE_KEY, value.toString()).apply()
}