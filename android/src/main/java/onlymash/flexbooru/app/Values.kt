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

package onlymash.flexbooru.app

object Values {
    const val DB_FILE_NAME = "flexbooru.db"

    const val BASE_URL = "http://fiepi.me"

    const val DATE_PATTERN = "yyyy-MM-dd HH:mm"
    const val DATE_PATTERN_DAN = "yyyy-MM-dd'T'HH:mm:ss.sss"
    const val DATE_PATTERN_GEL = "EEE MMM dd HH:mm:ss Z yyyy"
    const val DATE_PATTERN_SHIMMIE = "yyyy-MM-dd HH:mm:ss"

    const val SCHEME_HTTP = "http"
    const val SCHEME_HTTPS = "https"

    const val BOORU_TYPE_DAN = 0
    const val BOORU_TYPE_MOE = 1
    const val BOORU_TYPE_DAN1 = 2
    const val BOORU_TYPE_GEL = 3
    const val BOORU_TYPE_GEL_LEGACY = 6
    const val BOORU_TYPE_SANKAKU = 4
    const val BOORU_TYPE_SHIMMIE = 5
    const val BOORU_TYPE_UNKNOWN = -1

    const val PAGE_TYPE_POSTS = 0
    const val PAGE_TYPE_POPULAR = 1

    const val HASH_SALT_CONTAINED = "your-password"

    const val PC_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36"
    const val MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 13; KB2000) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Mobile Safari/537.36"
    const val SANKAKU_REFERER = "https://sankaku.app/"
    const val SANKAKU_ORIGIN = "https://sankaku.app"

    object Tags {
        const val TYPE_ALL = -1
        const val TYPE_GENERAL = 0
        const val TYPE_ARTIST = 1
        const val TYPE_COPYRIGHT = 3
        const val TYPE_CHARACTER = 4
        const val TYPE_CIRCLE = 5
        const val TYPE_FAULTS = 6
        const val TYPE_META = 5
        const val TYPE_MODEL = 5
        const val TYPE_PHOTO_SET = 6
        const val TYPE_META_SANKAKU = 9
        const val TYPE_STUDIO = 2
        const val TYPE_GENRE = 5
        const val TYPE_MEDIUM = 8
    }
}