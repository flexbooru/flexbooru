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
    }

    var activeBooruUid: Long
        get() = sp.getLong(Constants.ACTIVE_BOORU_UID_KEY, -1)
        set(value) = sp.edit().putLong(Constants.ACTIVE_BOORU_UID_KEY, value).apply()
}