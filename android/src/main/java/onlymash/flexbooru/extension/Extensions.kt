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

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.StaticLayout
import android.widget.Toast
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.postDelayed
import onlymash.flexbooru.R

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

fun Context.safeOpenIntent(intent: Intent) {
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {}
}

private fun getCustomTabsIntent(context: Context): CustomTabsIntent {
    return CustomTabsIntent.Builder()
        .setDefaultColorSchemeParams(CustomTabColorSchemeParams.Builder()
            .setToolbarColor(ContextCompat.getColor(context, R.color.colorBackground))
            .build())
        .build()
}

fun Context.launchUrl(uri: Uri) = try {
    getCustomTabsIntent(this).launchUrl(this, uri)
} catch (e: ActivityNotFoundException) { e.printStackTrace() }

fun Context.launchUrl(url: String) = this.launchUrl(Uri.parse(url))


fun Context.redirectToDownloadManagerSettings() {
    try {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:com.android.providers.downloads")
        startActivity(intent)
        Toast.makeText(this, getString(R.string.msg_download_must_enable), Toast.LENGTH_LONG).show()
    } catch (ex: ActivityNotFoundException) {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        startActivity(intent)
        Toast.makeText(this, getString(R.string.msg_download_must_enable), Toast.LENGTH_LONG).show()
    }
}

fun Context.openAppInMarket(packageName: String) {
    openUri("market://details?id=$packageName".toUri())
}

fun Context.openUrl(url: String) {
    openUri(url.toUri())
}

fun Context.openUri(uri: Uri) {
    try {
        startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW, uri), getString(R.string.share_via)))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, getString(R.string.msg_no_app_to_open_url), Toast.LENGTH_LONG).show()
    }
}

fun Context.downloadByAdm(url: String) {
    val intent = Intent().apply {
        action = Intent.ACTION_MAIN
        setPackage("com.dv.adm")
        component = ComponentName("com.dv.adm", "com.dv.get.AEditor")
        putExtra(Intent.EXTRA_TEXT, url)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        openUrl(url)
    }
}