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

package onlymash.flexbooru.worker

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import coil.executeBlocking
import coil.imageLoader
import coil.request.ImageRequest
import onlymash.flexbooru.app.App
import onlymash.flexbooru.app.Settings
import onlymash.flexbooru.okhttp.ProgressInterceptor
import java.io.FileInputStream
import java.io.IOException
import onlymash.flexbooru.R
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.extension.*
import onlymash.flexbooru.receiver.DownloadNotificationClickReceiver
import onlymash.flexbooru.okhttp.OkHttp3Downloader
import onlymash.flexbooru.ui.base.PathActivity
import java.io.InputStream

class DownloadWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    companion object {
        private const val URL_KEY = "url"
        private const val HOST_KEY = "host"
        private const val POST_ID_KEY = "post_id"

        private const val TYPE_KEY = "type"
        private const val TYPE_POST = "type_post"
        private const val TYPE_POOL = "type_pool"
        private const val SCHEME_KEY = "scheme"
        private const val POOL_ID_KEY = "pool_id"
        private const val POOL_DOWNLOAD_TYPE_KEY = "pool_download_type"
        const val POOL_DOWNLOAD_TYPE_JPGS = 0
        const val POOL_DOWNLOAD_TYPE_PNGS = 1
        private const val USERNAME_KEY = "username"
        private const val PASSWORD_HASH_KEY = "password_hash"

        const val DOC_ID_KEY = "doc_id"
        const val INPUT_DATA_KEY = "input_data"

        fun downloadPost(post: Post?, host: String, activity: PathActivity) {
            if (post == null) return
            val url = when (Settings.downloadSize) {
                Settings.POST_SIZE_SAMPLE -> post.sample
                Settings.POST_SIZE_LARGER -> post.medium
                else -> post.origin
            }
            if (url.isEmpty()) return
            var fileName = url.fileName()
            if (!fileName.contains(' ')) fileName = "${post.id} - $fileName"
            val docUri = activity.getDownloadUri(host, fileName) ?: return
            val docId = DocumentsContract.getDocumentId(docUri)
            val data = workDataOf(
                URL_KEY to url,
                HOST_KEY to host,
                POST_ID_KEY to post.id,
                TYPE_KEY to TYPE_POST,
                DOC_ID_KEY to docId
            )
            runWork(data)
        }

        fun download(url: String, postId: Int, host: String, activity: PathActivity) {
            if (url.isEmpty()) return
            var fileName = url.fileName()
            if (!fileName.contains(' ')) fileName = "$postId - $fileName"
            val docUri = activity.getDownloadUri(host, fileName) ?: return
            val docId = DocumentsContract.getDocumentId(docUri)
            val data = workDataOf(
                URL_KEY to url,
                HOST_KEY to host,
                POST_ID_KEY to postId,
                TYPE_KEY to TYPE_POST,
                DOC_ID_KEY to docId
            )
            runWork(data)
        }

        fun downloadPool(
            activity: PathActivity,
            poolId: Int,
            type: Int,
            booru: Booru
        ) {
            val scheme = booru.scheme
            val host = booru.host
            val fileName = when (type) {
                POOL_DOWNLOAD_TYPE_JPGS -> {
                    "$host - $poolId.jpg.zip"
                }
                else -> {
                    "$host - $poolId.png.zip"
                }
            }
            val docUri = activity.getPoolUri(fileName) ?: return
            val docId = DocumentsContract.getDocumentId(docUri)
            val data = workDataOf(
                SCHEME_KEY to scheme,
                HOST_KEY to host,
                POOL_ID_KEY to poolId,
                POOL_DOWNLOAD_TYPE_KEY to type,
                TYPE_KEY to TYPE_POOL,
                USERNAME_KEY to booru.user?.name,
                PASSWORD_HASH_KEY to booru.user?.token,
                DOC_ID_KEY to docId
            )
            runWork(data)
        }
        fun runWork(data: Data) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val work = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(App.app).enqueue(work)
        }
    }

    override suspend fun doWork(): Result {
        return when (inputData.getString(TYPE_KEY)) {
            TYPE_POST -> downloadPost()
            TYPE_POOL -> downloadPool()
            else -> Result.failure()
        }
    }

    private fun downloadPost(): Result {
        val url = inputData.getString(URL_KEY)
        val id = inputData.getInt(POST_ID_KEY, -1)
        val host = inputData.getString(HOST_KEY)
        val docId =  inputData.getString(DOC_ID_KEY)
        if (url == null || id < 0 || host == null || docId == null) return Result.failure()
        val desUri = applicationContext.contentResolver.getFileUriByDocId(docId) ?: return Result.failure()
        val channelId = applicationContext.packageName + ".download"
        val notificationManager = getNotificationManager(
            channelId,
            applicationContext.getString(R.string.post_download))
        val title = "$host - $id"
        ProgressInterceptor.bindUrlWithInterval(url, 500L) { progress ->
            setForegroundAsync(createDownloadingInfo(title, url, channelId, id, progress))
        }
        val file = try {
            val request = ImageRequest.Builder(applicationContext)
                .data(url)
                .memoryCacheKey(url)
                .diskCacheKey(url)
                .build()
            applicationContext.imageLoader.executeBlocking(request)
            applicationContext.imageLoader.diskCache?.get(url)?.data?.toFile()
        } catch (_: Exception) {
            null
        }
        ProgressInterceptor.removeListener(url)
        if (file == null || !file.exists()) {
            notificationManager.notify(id + 1000000, getDownloadErrorNotification(title, channelId))
            return Result.failure()
        }
        val `is` = FileInputStream(file)
        val os = applicationContext.contentResolver.openOutputStream(desUri)
        try {
            `is`.copyTo(os)
        } catch (_: IOException) {
            return Result.failure()
        } finally {
            `is`.safeCloseQuietly()
            os?.safeCloseQuietly()
        }
        notificationManager.notify(id + 1000000, getDownloadedNotification(title = title, channelId = channelId, desUri = desUri))
        return Result.success()
    }

    private fun downloadPool(): Result {
        val id = inputData.getInt(POOL_ID_KEY, -1)
        val scheme = inputData.getString(SCHEME_KEY)
        val host = inputData.getString(HOST_KEY)
        val type = inputData.getInt(
            POOL_DOWNLOAD_TYPE_KEY,
            POOL_DOWNLOAD_TYPE_JPGS
        )
        val username = inputData.getString(USERNAME_KEY)
        val passwordHash = inputData.getString(PASSWORD_HASH_KEY)
        val docId =  inputData.getString(DOC_ID_KEY)
        if (id < 0 || scheme.isNullOrEmpty() ||
            host.isNullOrEmpty() || username.isNullOrEmpty() ||
            passwordHash.isNullOrEmpty() || docId == null)
            return Result.failure()
        var url = when (type) {
            POOL_DOWNLOAD_TYPE_JPGS -> {
                applicationContext.getString(R.string.pool_download_jpgs_url_format, scheme, host, id)
            }
            else -> {
                applicationContext.getString(R.string.pool_download_pngs_url_format, scheme, host, id)
            }
        }
        url = "$url&login=$username&password_hash=$passwordHash"
        val desUri = applicationContext.contentResolver.getFileUriByDocId(docId) ?: return Result.failure()
        val channelId = applicationContext.packageName + ".download_pool"
        val notificationManager = getNotificationManager(
            channelId,
            applicationContext.getString(R.string.pool_download))
        val title = "$host - pool: $id"
        ProgressInterceptor.bindUrlWithInterval(url, 500L) { progress ->
            setForegroundAsync(createDownloadingInfo(title, url, channelId, id, progress))
        }
        var `is`: InputStream? = null
        val os = applicationContext.contentResolver.openOutputStream(desUri)
        try {
            `is` = OkHttp3Downloader(applicationContext).load(url).body.source().inputStream()
            `is`.copyTo(os)
        } catch (_: IOException) {
            return Result.failure()
        } finally {
            ProgressInterceptor.removeListener(url)
            `is`?.safeCloseQuietly()
            os?.safeCloseQuietly()
        }
        notificationManager.notify(id + 1000000, getDownloadedNotification(title = title, channelId = channelId, desUri = desUri))
        return Result.success()
    }

    private fun getNotificationManager(channelId: String, channelName: String): NotificationManager {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        return notificationManager
    }

    private fun createDownloadingInfo(title: String, url: String, channelId: String, notificationId: Int, progress: Int): ForegroundInfo {
        val cancelIntent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setContentTitle(title)
            .setContentText(url)
            .setOngoing(true)
            .setAutoCancel(false)
            .setShowWhen(false)
            .addAction(android.R.drawable.ic_delete, applicationContext.getString(R.string.dialog_cancel), cancelIntent)
            .setProgress(100, progress, false)
            .build()
        return ForegroundInfo(notificationId, notification)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getDownloadedNotification(title: String, channelId: String, desUri: Uri): Notification {
        val intent = Intent(applicationContext, DownloadNotificationClickReceiver::class.java)
        intent.data = desUri
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                applicationContext,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getBroadcast(
                applicationContext,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT)
        }
        return NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
            .setContentTitle(title)
            .setContentText(applicationContext.getString(R.string.msg_download_complete))
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getDownloadErrorNotification(title: String, channelId: String): Notification {
        val intent = Intent(applicationContext, DownloadNotificationClickReceiver::class.java)
        intent.putExtra(INPUT_DATA_KEY, inputData.toByteArray())
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                applicationContext,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getBroadcast(
                applicationContext,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT)
        }
        return NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
            .setContentTitle(title)
            .setContentText(applicationContext.getString(R.string.msg_download_failed))
            .setOngoing(false)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.stat_sys_download,
                applicationContext.getString(R.string.action_retry),
                pendingIntent
            )
            .build()
    }
}