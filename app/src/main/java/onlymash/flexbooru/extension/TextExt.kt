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

package onlymash.flexbooru.extension

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context


private val REGEX_HOST = "^(?=.{1,255}\$)[0-9A-Za-z](?:(?:[0-9A-Za-z]|-){0,61}[0-9A-Za-z])?(?:\\.[0-9A-Za-z](?:(?:[0-9A-Za-z]|-){0,61}[0-9A-Za-z])?)*\\.?\$".toRegex()

private val REGEX_NUMBER = "-?\\d+(\\.\\d+)?".toRegex()

fun String.isHost(): Boolean = matches(REGEX_HOST)

fun String.isNumber(): Boolean = matches(REGEX_NUMBER)

fun Context.copyText(text: String?) {
    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    cm.primaryClip = ClipData.newPlainText("text", text ?: "")
}