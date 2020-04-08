/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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

package onlymash.flexbooru.data.utils

import onlymash.flexbooru.common.Values.DATE_PATTERN_SHIMMIE
import onlymash.flexbooru.common.Values.DATE_PATTERN_DAN as PATTERN_DAN
import onlymash.flexbooru.common.Values.DATE_PATTERN_GEL as PATTERN_GEL
import onlymash.flexbooru.extension.formatDate
import java.text.SimpleDateFormat
import java.util.*

fun String.toSafeUrl(scheme: String, host: String): String {
    var url = this
    if (contains("""\/""")) {
        url = url.replace("""\/""", "/")
    }
    return when {
        url.startsWith("http") -> url
        url.startsWith("//") -> "$scheme:$url"
        url.startsWith("/") -> "$scheme://$host$url"
        else -> url
    }
}

fun String.formatDateDan(): CharSequence? =
    SimpleDateFormat(PATTERN_DAN, Locale.ENGLISH)
        .parse(this)?.time?.formatDate()

fun String.formatDateGel(): CharSequence? =
    SimpleDateFormat(PATTERN_GEL, Locale.ENGLISH)
        .parse(this)?.time?.formatDate()

fun String.formatDateShimmie(): CharSequence? =
    SimpleDateFormat(DATE_PATTERN_SHIMMIE, Locale.ENGLISH)
        .parse(this)?.time?.formatDate()
