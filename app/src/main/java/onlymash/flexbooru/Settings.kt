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

package onlymash.flexbooru

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import onlymash.flexbooru.App.Companion.app

class Settings(private val sp: SharedPreferences) {
    companion object {
        private var instance: Settings? = null
        /**
         * return [Settings] instance
         * */
        fun instance(): Settings {
            if (instance == null) {
                instance = Settings(app.sp)
            }
            return instance!!
        }
        const val SAFE_MODE_KEY = "settings_safe_mode"
        const val PAGE_LIMIT_KEY = "settings_page_limit"
        const val MUZEI_LIMIT_KEY = "settings_muzei_limit"
        const val BROWSE_SIZE_KEY = "settings_browse_size"
        const val DOWNLOAD_SIZE_KEY = "settings_download_size"
        const val DOWNLOAD_PATH_KEY = "settings_download_path"
        const val DOWNLOAD_PATH_TREE_ID_KEY = "settings_download_path_tree_id"
        const val DOWNLOAD_PATH_AUTHORITY_KEY = "settings_download_path_authority"
        const val NIGHT_MODE_KEY = "settings_night_mode"
        const val THEME_KEY = "settings_theme"
        const val MUZEI_SIZE_KEY = "settings_muzei_size"
        const val POST_SIZE_SAMPLE = "sample"
        const val POST_SIZE_LARGER = "larger"
        const val POST_SIZE_ORIGIN = "origin"
        const val GRID_WIDTH_KEY = "settings_grid_width"
        const val GRID_WIDTH_SMALL = "small"
        const val GRID_WIDTH_NORMAL = "normal"
        const val GRID_WIDTH_LARGE = "large"
        private const val ACTIVE_MUZEI_UID_KEY = "settings_muzei_uid"
        const val SHOW_INFO_BAR_KEY = "settings_show_info_bar"
        const val CLEAR_CACHE_KEY = "settings_clear_cache"
        const val CLEAR_HISTORY_KEY = "settings_clear_history"
    }

    var activeBooruUid: Long
        get() = sp.getLong(Constants.ACTIVE_BOORU_UID_KEY, -1)
        set(value) = sp.edit().putLong(Constants.ACTIVE_BOORU_UID_KEY, value).apply()

    var safeMode: Boolean
        get() = sp.getBoolean(SAFE_MODE_KEY, true)
        set(value) = sp.edit().putBoolean(SAFE_MODE_KEY, value).apply()

    var pageLimit: Int
        get() = sp.getString(PAGE_LIMIT_KEY, "10")!!.toInt()
        set(value) = sp.edit().putString(PAGE_LIMIT_KEY, value.toString()).apply()

    var muzeiLimit: Int
        get() = sp.getString(MUZEI_LIMIT_KEY, "10")!!.toInt()
        set(value) = sp.edit().putString(MUZEI_LIMIT_KEY, value.toString()).apply()

    var browseSize: String
        get() = sp.getString(BROWSE_SIZE_KEY, POST_SIZE_SAMPLE) ?: POST_SIZE_SAMPLE
        set(value) = sp.edit().putString(BROWSE_SIZE_KEY, value).apply()

    var downloadSize: String
        get() = sp.getString(DOWNLOAD_SIZE_KEY, POST_SIZE_SAMPLE) ?: POST_SIZE_LARGER
        set(value) = sp.edit().putString(DOWNLOAD_SIZE_KEY, value).apply()

    var muzeiSize: String
        get() = sp.getString(MUZEI_SIZE_KEY, POST_SIZE_SAMPLE) ?: POST_SIZE_LARGER
        set(value) = sp.edit().putString(MUZEI_SIZE_KEY, value).apply()

    var isNightMode: Boolean
        get() = sp.getBoolean(NIGHT_MODE_KEY, false)
        set(value) = sp.edit().putBoolean(NIGHT_MODE_KEY, value).apply()

    @AppCompatDelegate.NightMode
    val nightMode: Int
        get() = if (isNightMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }


    var gridWidth: String
        get() = sp.getString(GRID_WIDTH_KEY, GRID_WIDTH_NORMAL) ?: GRID_WIDTH_NORMAL
        set(value) = sp.edit().putString(GRID_WIDTH_KEY, value).apply()

    var activeMuzeiUid: Long
        get() = sp.getLong(ACTIVE_MUZEI_UID_KEY, 0L)
        set(value) = sp.edit().putLong(ACTIVE_MUZEI_UID_KEY, value).apply()

    var showInfoBar: Boolean
        get() = sp.getBoolean(SHOW_INFO_BAR_KEY, false)
        set(value) = sp.edit().putBoolean(SHOW_INFO_BAR_KEY, value).apply()

    var downloadDirPath: String?
        get() = sp.getString(DOWNLOAD_PATH_KEY, "")
        set(value) = sp.edit().putString(DOWNLOAD_PATH_KEY, value).apply()

    var downloadDirPathTreeId: String?
        get() = sp.getString(DOWNLOAD_PATH_TREE_ID_KEY, "")
        set(value) = sp.edit().putString(DOWNLOAD_PATH_TREE_ID_KEY, value).apply()

    var downloadDirPathAuthority: String?
        get() = sp.getString(DOWNLOAD_PATH_AUTHORITY_KEY, "")
        set(value) = sp.edit().putString(DOWNLOAD_PATH_AUTHORITY_KEY, value).apply()
}