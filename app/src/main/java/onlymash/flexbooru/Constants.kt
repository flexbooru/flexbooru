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

package onlymash.flexbooru

object Constants {
    const val DB_FILE_NAME = "Flexbooru.db"

    const val USER_AGENT_KEY = "User-Agent"
    const val REFERER_KEY = "Referer"
    const val BASE_URL = "http://fiepi.me"

    const val SCHEME_KEY = "scheme"
    const val HOST_KEY = "host"
    const val KEYWORD_KEY = "tags"
    const val ID_KEY = "id"
    const val AUTH_KEY = "auth_key"
    const val USERNAME_KEY = "username"
    const val USER_ID_KEY = "user_id"

    const val TYPE_KEY = "type"
    const val TYPE_DANBOORU = 0
    const val TYPE_MOEBOORU = 1
    const val TYPE_DANBOORU_ONE = 2
    const val TYPE_GELBOORU = 3
    const val TYPE_UNKNOWN = -1

    const val MAX_ITEM_ASPECT_RATIO = 1.3333f
    const val MIN_ITEM_ASPECT_RATIO = 0.5625f

    const val REQUEST_EDIT_CODE = 0
    const val REQUEST_ADD_CODE = 1
    const val EXTRA_RESULT_KEY = "activity_result"
    const val RESULT_DELETE = "booru_delete"
    const val RESULT_ADD = "booru_add"
    const val RESULT_UPDATE = "booru_update"

    const val URL_KEY = "url"

    const val BOORU_HELP_URL = "https://github.com/flexbooru/flexbooru/wiki/import-booru"

    const val ACTIVE_BOORU_UID_KEY = "active_booru_uid"

    const val HASH_SALT_CONTAINED = "your-password"

    const val PAGE_TYPE_KEY = "page_type"
    const val PAGE_TYPE_POST = 0
    const val PAGE_TYPE_POPULAR = 1
}