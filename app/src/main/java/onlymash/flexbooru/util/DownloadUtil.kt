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

package onlymash.flexbooru.util

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.*
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import onlymash.flexbooru.Settings
import onlymash.flexbooru.entity.post.PostBase
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.glide.ProgressInterceptor
import onlymash.flexbooru.glide.ProgressListener
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import onlymash.flexbooru.R
import onlymash.flexbooru.receiver.DownloadNotificationClickReceiver

class DownloadUtil(
    context: Context,
    workerParameters: WorkerParameters
) : Worker(context, workerParameters) {

    companion object {
        private const val URL_KEY = "url"
        private const val HOST_KEY = "host"
        private const val POST_ID_KEY = "post_id"
        private const val FILENAME_KEY = "filename"
        private const val PATH_KEY = "path"
        const val EXT_DOWNLOADED = ":downloaded"

        internal fun downloadPost(post: PostBase?, activity: Activity) {
            if (post == null) return
            val host = post.host
            val id = post.getPostId()
            val url = when (Settings.instance().downloadSize) {
                Settings.POST_SIZE_SAMPLE -> post.getSampleUrl()
                Settings.POST_SIZE_LARGER -> post.getLargerUrl()
                else -> post.getOriginUrl()
            }
            if (url.isEmpty()) return
            var fileName = Uri.decode(url.fileName())
            if (!fileName.contains(' ')) fileName = "$id - $fileName"
            val desPath = activity.getDownloadUri(host, fileName)?.toString() ?: return
            val workManager = WorkManager.getInstance()
            workManager.enqueue(
                OneTimeWorkRequestBuilder<DownloadUtil>()
                    .setInputData(
                        workDataOf(
                            URL_KEY to url,
                            HOST_KEY to host,
                            POST_ID_KEY to id,
                            FILENAME_KEY to fileName,
                            PATH_KEY to desPath
                        )
                    )
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .build()
            )
        }
    }

    override fun doWork(): Result {
        val url = inputData.getString(URL_KEY)
        val id = inputData.getInt(POST_ID_KEY, -1)
        val host = inputData.getString(HOST_KEY)
        val filename = inputData.getString(FILENAME_KEY)
        val path = Uri.decode(inputData.getString(PATH_KEY))
        if (url == null || id < 0 || host == null || filename == null || path == null) return Result.failure()
        val desUri = if (path.startsWith(ContentResolver.SCHEME_CONTENT)) path.safeStringToUri() else File(path).toUri()
        val channelId = applicationContext.packageName + ".download"
        val notificationManager = getNotificationManager(channelId)
        val title = "$host - $id"
        val downloadingNotificationBuilder = getDownloadingNotificationBuilder(title = title, url = url, channelId = channelId)
        var startTime = 0L
        var elapsedTime = 500L
        ProgressInterceptor.addListener(url, object : ProgressListener {
            override fun onProgress(progress: Int) {
                if (elapsedTime >= 500L) {
                    downloadingNotificationBuilder.setProgress(100, progress, false)
                    notificationManager.notify(id, downloadingNotificationBuilder.build())
                    startTime = System.currentTimeMillis()
                    elapsedTime = 0L
                } else {
                    elapsedTime = System.currentTimeMillis() - startTime
                }
            }
        })
        val file = GlideApp.with(applicationContext)
            .downloadOnly()
            .load(url)
            .listener(object : RequestListener<File> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<File>?,
                    isFirstResource: Boolean
                ): Boolean {
                    ProgressInterceptor.removeListener(url)
                    notificationManager.notify(id, getDownloadErrorNotificationBuilder(title, channelId).build())
                    return false
                }
                override fun onResourceReady(
                    resource: File?,
                    model: Any?,
                    target: Target<File>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    ProgressInterceptor.removeListener(url)
                    return false
                }

            })
            .submit()
            .get()
        if (file == null || !file.exists()) {
            notificationManager.notify(id, getDownloadErrorNotificationBuilder(title, channelId).build())
            return Result.failure()
        }
        notificationManager.notify(id, getDownloadedNotificationBuilder(title = title, channelId = channelId, path = path).build())
        val `is` = FileInputStream(file)
        val os = applicationContext.contentResolver.openOutputStream(desUri)
        try {
            IOUtils.copy(`is`, os)
        } catch (_: IOException) {
            return Result.failure()
        } finally {
            IOUtils.closeQuietly(`is`)
            IOUtils.closeQuietly(os)
        }
        return Result.success()
    }

    private fun getNotificationManager(channelId: String): NotificationManager {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                applicationContext.getString(R.string.post_download),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        return notificationManager
    }

    private fun getDownloadingNotificationBuilder(title: String, url: String, channelId: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setContentTitle(title)
            .setContentText(url)
            .setOngoing(true)
            .setAutoCancel(false)
            .setShowWhen(false)
    }

    private fun getDownloadedNotificationBuilder(title: String, channelId: String, path: String): NotificationCompat.Builder {
        val intent = Intent(applicationContext, DownloadNotificationClickReceiver::class.java)
            .putExtra(applicationContext.packageName + EXT_DOWNLOADED, path)
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, System.currentTimeMillis().toInt(), intent, 0)
        return NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(title)
            .setContentText(applicationContext.getString(R.string.msg_download_complete))
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
    }

    private fun getDownloadErrorNotificationBuilder(title: String, channelId: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(title)
            .setContentText(applicationContext.getString(R.string.msg_download_failed))
            .setOngoing(false)
            .setAutoCancel(true)
    }

}