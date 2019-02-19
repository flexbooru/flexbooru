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

package onlymash.flexbooru.util

import android.app.Activity
import android.content.res.Resources
import android.net.wifi.WifiConfiguration
import android.text.StaticLayout
import android.util.TypedValue
import androidx.annotation.DimenRes
import androidx.core.view.postDelayed

/**
 * An extension to `postponeEnterTransition` which will resume after a timeout.
 */
fun Activity.postponeEnterTransition(timeout: Long) {
    postponeEnterTransition()
    window.decorView.postDelayed(timeout) {
        startPostponedEnterTransition()
    }
}

/**
 * Calculated the widest line in a [StaticLayout].
 */
fun StaticLayout.textWidth(): Int {
    var width = 0f
    for (i in 0 until lineCount) {
        width = width.coerceAtLeast(getLineWidth(i))
    }
    return width.toInt()
}

/**
 * Linearly interpolate between two values.
 */
fun lerp(a: Float, b: Float, t: Float): Float {
    return a + (b - a) * t
}

/**
 * Alternative to Resources.getDimension() for values that are TYPE_FLOAT.
 */
fun Resources.getFloat(@DimenRes resId: Int): Float {
    val outValue = TypedValue()
    getValue(resId, outValue, true)
    return outValue.float
}

/**
 * Return the Wifi config wrapped in quotes.
 */
fun WifiConfiguration.quoteSsidAndPassword(): WifiConfiguration {
    return WifiConfiguration().apply {
        SSID = this@quoteSsidAndPassword.SSID.wrapInQuotes()
        preSharedKey = this@quoteSsidAndPassword.preSharedKey.wrapInQuotes()
    }
}

/**
 * Return the Wifi config without quotes.
 */
fun WifiConfiguration.unquoteSsidAndPassword(): WifiConfiguration {
    return WifiConfiguration().apply {
        SSID = this@unquoteSsidAndPassword.SSID.unwrapQuotes()
        preSharedKey = this@unquoteSsidAndPassword.preSharedKey.unwrapQuotes()
    }
}

fun String.wrapInQuotes(): String {
    var formattedConfigString: String = this
    if (!startsWith("\"")) {
        formattedConfigString = "\"$formattedConfigString"
    }
    if (!endsWith("\"")) {
        formattedConfigString = "$formattedConfigString\""
    }
    return formattedConfigString
}

fun String.unwrapQuotes(): String {
    var formattedConfigString: String = this
    if (formattedConfigString.startsWith("\"")) {
        if (formattedConfigString.length > 1) {
            formattedConfigString = formattedConfigString.substring(1)
        } else {
            formattedConfigString = ""
        }
    }
    if (formattedConfigString.endsWith("\"")) {
        if (formattedConfigString.length > 1) {
            formattedConfigString =
                formattedConfigString.substring(0, formattedConfigString.length - 1)
        } else {
            formattedConfigString = ""
        }
    }
    return formattedConfigString
}