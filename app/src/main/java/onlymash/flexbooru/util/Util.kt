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
import android.content.res.Resources
import android.net.Uri
import android.os.Environment
import android.text.format.DateFormat
import android.util.DisplayMetrics
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.customview.widget.ViewDragHelper
import androidx.drawerlayout.widget.DrawerLayout
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.Settings
import onlymash.flexbooru.entity.post.BasePost
import java.io.File
import java.lang.IllegalArgumentException
import java.net.URLDecoder
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

fun Context.downloadPost(post: BasePost?) {
    if (post == null) return
    val host = post.host
    val id = post.getPostId()
    val url = when (Settings.instance().downloadSize) {
        Settings.POST_SIZE_SAMPLE -> post.getSampleUrl()
        Settings.POST_SIZE_LARGER -> post.getLargerUrl()
        else -> post.getOriginUrl()
    }
    if (url.isEmpty()) return
    val fileName = URLDecoder.decode(url.substring(url.lastIndexOf("/") + 1), "UTF-8")
    val path = File(Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_PICTURES),
        String.format("%s/%s/%s", getString(R.string.app_name), host, fileName))
    val uri = Uri.parse(url)
    val request = DownloadManager.Request(uri).apply {
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        setTitle(String.format("%s - %d", host, id))
        setDescription(fileName)
        setDestinationUri(Uri.fromFile(path))
        addRequestHeader(Constants.USER_AGENT_KEY, UserAgent.get())
        addRequestHeader(Constants.REFERER_KEY, "${uri.scheme}://${uri.host}/post")
    }
    (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
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