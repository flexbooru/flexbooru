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

package onlymash.flexbooru.extension

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import onlymash.flexbooru.app.Values.DATE_PATTERN
import java.text.SimpleDateFormat
import java.util.*

fun Long.formatDate(pattern: String = DATE_PATTERN): CharSequence {
    val cal = Calendar.getInstance(Locale.getDefault())
    cal.timeInMillis = this
    return DateFormat.format(pattern, cal)
}

fun String.parseDate(pattern: String = DATE_PATTERN): Long? {
    val sdf = SimpleDateFormat(pattern, Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.parse(this)?.time
}

fun Context.formatDate(millis: Long?): String? {
    if (millis == null) {
        return null
    }
    return DateUtils.formatDateTime(
        this,
        millis,
        DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR
    )
}