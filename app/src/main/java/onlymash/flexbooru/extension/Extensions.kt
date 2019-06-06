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

import android.app.Activity
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.text.StaticLayout
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.postDelayed
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Constants
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.database.CookieManager
import onlymash.flexbooru.entity.post.PostBase

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

fun Window.showBar() {
    val uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    decorView.systemUiVisibility = uiFlags
}

fun Window.hideBar() {
    val uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE
    decorView.systemUiVisibility = uiFlags
}

fun Activity.getWindowWidth(): Int {
    val outMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(outMetrics)
    return outMetrics.widthPixels
}

fun Context.safeOpenIntent(intent: Intent) {
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {}
}

private fun getCustomTabsIntent(context: Context): CustomTabsIntent {
    return CustomTabsIntent.Builder()
        .setToolbarColor(ContextCompat.getColor(context, R.color.colorBackground))
        .build()
}

fun Context.launchUrl(uri: Uri) = try {
    getCustomTabsIntent(this).launchUrl(this, uri)
} catch (e: ActivityNotFoundException) { e.printStackTrace() }

fun Context.launchUrl(url: String) = this.launchUrl(Uri.parse(url))

fun Resources.gridWidth() = when (Settings.gridWidth) {
    Settings.GRID_WIDTH_SMALL -> getDimensionPixelSize(R.dimen.post_item_width_small)
    Settings.GRID_WIDTH_NORMAL -> getDimensionPixelSize(R.dimen.post_item_width_normal)
    else -> getDimensionPixelSize(R.dimen.post_item_width_large)
}

fun Activity.downloadPost(post: PostBase?) {
    if (post == null) return
    var host = post.host
    val id = post.getPostId()
    val url = when (Settings.downloadSize) {
        Settings.POST_SIZE_SAMPLE -> post.getSampleUrl()
        Settings.POST_SIZE_LARGER -> post.getLargerUrl()
        else -> post.getOriginUrl()
    }
    if (url.isEmpty()) return
    var fileName = url.fileName()
    if (!fileName.contains(' ')) fileName = "${post.getPostId()} - $fileName"
    val uri = getDownloadUri(host, fileName)
    val request = DownloadManager.Request(Uri.parse(url)).apply {
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        setTitle(String.format("%s - %d", host, id))
        setDescription(fileName)
        setDestinationUri(uri)
        addRequestHeader(Constants.USER_AGENT_KEY, getUserAgent())
        if (host.startsWith("capi-v2.")) host = host.replaceFirst("capi-v2.", "beta.")
        addRequestHeader(Constants.REFERER_KEY, "${post.scheme}://$host/post")
        CookieManager.getCookieByBooruUid(Settings.activeBooruUid)?.cookie?.let { cookie ->
            addRequestHeader("Cookie", cookie)
        }
    }
    try {
        (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
    } catch (ex: Exception) {
        redirectToDownloadManagerSettings()
    }
}

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

fun Activity.getWidth(): Int {
    val outMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(outMetrics)
    return outMetrics.widthPixels
}

fun Activity.openAppInMarket(packageName: String) {
    try {
        startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")),
                getString(R.string.share_via)))
    } catch (_: ActivityNotFoundException) { }
}