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
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Environment
import android.text.format.DateFormat
import android.util.DisplayMetrics
import android.view.View
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.customview.widget.ViewDragHelper
import androidx.drawerlayout.widget.DrawerLayout
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.Settings
import onlymash.flexbooru.database.CookieManager
import onlymash.flexbooru.entity.post.PostBase
import java.util.*

fun formatDate(time: Long): CharSequence {
    val cal = Calendar.getInstance(Locale.getDefault())
    cal.timeInMillis = time
    return DateFormat.format("yyyy-MM-dd HH:mm", cal)
}

private fun getCustomTabsIntent(context: Context): CustomTabsIntent {
    return CustomTabsIntent.Builder()
        .setToolbarColor(ContextCompat.getColor(context, R.color.background))
        .build()
}

fun Context.launchUrl(uri: Uri) = try {
    getCustomTabsIntent(this).launchUrl(this, uri)
} catch (e: ActivityNotFoundException) { e.printStackTrace() }

fun Context.launchUrl(url: String) = this.launchUrl(Uri.parse(url))

fun Resources.gridWidth() = when (Settings.instance().gridWidth) {
        Settings.GRID_WIDTH_SMALL -> getDimensionPixelSize(R.dimen.post_item_width_small)
        Settings.GRID_WIDTH_NORMAL -> getDimensionPixelSize(R.dimen.post_item_width_normal)
        else -> getDimensionPixelSize(R.dimen.post_item_width_large)
    }

fun Resources.getUri(resId: Int): Uri =
        Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(getResourcePackageName(resId))
            .appendPath(getResourceTypeName(resId))
            .appendPath(getResourceEntryName(resId))
            .build()

/* Checks if external storage is available for read and write */
fun isExternalStorageWritable(): Boolean {
    return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}

/* Checks if external storage is available to at least read */
fun isExternalStorageReadable(): Boolean {
    return Environment.getExternalStorageState() in
            setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
}

fun String.fileName(): String {
    val start = lastIndexOf('/') + 1
    val end = indexOfFirst { it == '?' }
    return if (end > start) {
        substring(start, end)
    } else {
        substring(start)
    }
}

fun Activity.downloadPost(post: PostBase?) {
    if (post == null) return
    var host = post.host
    val id = post.getPostId()
    val url = when (Settings.instance().downloadSize) {
        Settings.POST_SIZE_SAMPLE -> post.getSampleUrl()
        Settings.POST_SIZE_LARGER -> post.getLargerUrl()
        else -> post.getOriginUrl()
    }
    if (url.isEmpty()) return
    var fileName = Uri.decode(url.fileName())
    if (!fileName.contains(' ')) fileName = "${post.getPostId()} - $fileName"
    val uri = getDownloadUri(host, fileName)
    val request = DownloadManager.Request(Uri.parse(url)).apply {
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        setTitle(String.format("%s - %d", host, id))
        setDescription(fileName)
        setDestinationUri(uri)
        addRequestHeader(Constants.USER_AGENT_KEY, UserAgent.get())
        if (host.startsWith("capi-v2.")) host = host.replaceFirst("capi-v2.", "beta.")
        addRequestHeader(Constants.REFERER_KEY, "${post.scheme}://$host/post")
        CookieManager.getCookieByBooruUid(Settings.instance().activeBooruUid)?.cookie?.let { cookie ->
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

fun DrawerLayout.setDrawerLeftEdgeSize(activity: Activity, displayWidthPercentage: Float) {
    try {
        val leftDraggerField = this.javaClass.getDeclaredField("mLeftDragger")
        leftDraggerField.isAccessible = true
        val leftDragger = leftDraggerField.get(this) as ViewDragHelper
        val edgeSizeField = leftDragger.javaClass.getDeclaredField("mEdgeSize")
        edgeSizeField.isAccessible = true
        val edgeSize = edgeSizeField.getInt(leftDragger)
        val dm = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(dm)
        edgeSizeField.setInt(leftDragger, Math.max(edgeSize, (dm.widthPixels * displayWidthPercentage).toInt()))
    } catch (_: NoSuchFieldException) {

    } catch (_: IllegalAccessException) {

    } catch (_: IllegalArgumentException) {

    }
}

fun DrawerLayout.setDrawerLayoutSlideListener() {
    addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            super.onDrawerSlide(drawerView, slideOffset)
            val marginLeft = (getChildAt(1).width * slideOffset).toInt()
            getChildAt(0).left = marginLeft
        }
    })
}

fun Activity.getWidth(): Int {
    val outMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(outMetrics)
    return outMetrics.widthPixels
}