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

package onlymash.flexbooru.common

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import org.kodein.di.erased.instance
import java.util.*

object Settings {

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
    const val SHOW_ALL_TAGS = "settings_show_all_tags"
    const val CLEAR_CACHE_KEY = "settings_clear_cache"
    const val CLEAR_HISTORY_KEY = "settings_clear_history"
    const val LATEST_VERSION_CODE_KEY = "settings_latest_version_code"
    const val LATEST_VERSION_NAME_KEY = "settings_latest_version_name"
    const val LATEST_VERSION_URL_KEY = "settings_latest_version_url"
    private const val ORDER_ID_KEY = "order_id"
    private const val ORDER_TIME_KEY = "order_time"
    private const val ORDER_TOKEN_KEY = "order_token"
    const val ORDER_SUCCESS_KEY = "order_success"
    const val ORDER_DEVICE_ID_KEY = "device_id"
    private const val GOOGLE_SIGN_KEY = "google_sign"
    private const val IS_AVAILABLE_STORE = "is_available_store"

    private const val SAUCE_NAO_API_KEY_KEY = "sauce_nao_api_key"

    private val sp: SharedPreferences by App.app.instance()

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
        get() = sp.getString(
            BROWSE_SIZE_KEY,
            POST_SIZE_SAMPLE
        ) ?: POST_SIZE_SAMPLE
        set(value) = sp.edit().putString(BROWSE_SIZE_KEY, value).apply()

    var downloadSize: String
        get() = sp.getString(
            DOWNLOAD_SIZE_KEY,
            POST_SIZE_SAMPLE
        ) ?: POST_SIZE_LARGER
        set(value) = sp.edit().putString(DOWNLOAD_SIZE_KEY, value).apply()

    var muzeiSize: String
        get() = sp.getString(
            MUZEI_SIZE_KEY,
            POST_SIZE_SAMPLE
        ) ?: POST_SIZE_LARGER
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
        get() = sp.getString(
            GRID_WIDTH_KEY,
            GRID_WIDTH_NORMAL
        ) ?: GRID_WIDTH_NORMAL
        set(value) = sp.edit().putString(GRID_WIDTH_KEY, value).apply()

    var activeMuzeiUid: Long
        get() = sp.getLong(ACTIVE_MUZEI_UID_KEY, 0L)
        set(value) = sp.edit().putLong(ACTIVE_MUZEI_UID_KEY, value).apply()

    var showInfoBar: Boolean
        get() = sp.getBoolean(SHOW_INFO_BAR_KEY, false)
        set(value) = sp.edit().putBoolean(SHOW_INFO_BAR_KEY, value).apply()

    var isShowAllTags: Boolean
        get() = sp.getBoolean(SHOW_ALL_TAGS, false)
        set(value) = sp.edit().putBoolean(SHOW_ALL_TAGS, value).apply()

    var downloadDirPath: String?
        get() = sp.getString(DOWNLOAD_PATH_KEY, "")
        set(value) = sp.edit().putString(DOWNLOAD_PATH_KEY, value).apply()

    var downloadDirPathTreeId: String?
        get() = sp.getString(DOWNLOAD_PATH_TREE_ID_KEY, "")
        set(value) = sp.edit().putString(DOWNLOAD_PATH_TREE_ID_KEY, value).apply()

    var downloadDirPathAuthority: String?
        get() = sp.getString(DOWNLOAD_PATH_AUTHORITY_KEY, "")
        set(value) = sp.edit().putString(DOWNLOAD_PATH_AUTHORITY_KEY, value).apply()

    var latestVersionCode: Long
        get() = sp.getLong(LATEST_VERSION_CODE_KEY, -1L)
        set(value) = sp.edit().putLong(LATEST_VERSION_CODE_KEY, value).apply()

    var latestVersionName: String
        get() = sp.getString(LATEST_VERSION_NAME_KEY, " ") ?: " "
        set(value) = sp.edit().putString(LATEST_VERSION_NAME_KEY, value).apply()

    var latestVersionUrl: String
        get() = sp.getString(LATEST_VERSION_URL_KEY, "") ?: ""
        set(value) = sp.edit().putString(LATEST_VERSION_URL_KEY, value).apply()

    var isAvailableOnStore: Boolean
        get() = sp.getBoolean(IS_AVAILABLE_STORE, false)
        set(value) = sp.edit().putBoolean(IS_AVAILABLE_STORE, value).apply()

    var orderId: String
        get() = sp.getString(ORDER_ID_KEY, "") ?: ""
        set(value) = sp.edit().putString(ORDER_ID_KEY, value).apply()

    var orderTime: Long
        get() = sp.getLong(ORDER_TIME_KEY, -1L)
        set(value) = sp.edit().putLong(ORDER_TIME_KEY, value).apply()

    var orderToken: String
        get() = sp.getString(ORDER_TOKEN_KEY, "") ?: ""
        set(value) = sp.edit().putString(ORDER_TOKEN_KEY, value).apply()

    var isOrderSuccess: Boolean
        get() = sp.getBoolean(ORDER_SUCCESS_KEY, false)
        set(value) = sp.edit().putBoolean(ORDER_SUCCESS_KEY, value).apply()

    var orderDeviceId: String
        get() {
            var id = sp.getString(ORDER_DEVICE_ID_KEY, "") ?: ""
            if (id.isEmpty()) {
                id = UUID.randomUUID().toString()
                orderDeviceId = id
            }
            return id
        }
        set(value) = sp.edit().putString(ORDER_DEVICE_ID_KEY, value).apply()

    var isGoogleSign: Boolean
        get() = sp.getBoolean(GOOGLE_SIGN_KEY, false)
        set(value) = sp.edit().putBoolean(GOOGLE_SIGN_KEY, value).apply()

    var sauceNaoApiKey: String
        get() = sp.getString(SAUCE_NAO_API_KEY_KEY, "") ?: ""
        set(value) = sp.edit().putString(SAUCE_NAO_API_KEY_KEY, value).apply()
}