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

package onlymash.flexbooru.worker

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import onlymash.flexbooru.common.App
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.entity.post.PostBase
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.okhttp.ProgressInterceptor
import onlymash.flexbooru.okhttp.ProgressListener
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import onlymash.flexbooru.R
import onlymash.flexbooru.entity.pool.PoolMoe
import onlymash.flexbooru.extension.getDownloadUri
import onlymash.flexbooru.extension.getFileUriByDocId
import onlymash.flexbooru.extension.getPoolUri
import onlymash.flexbooru.receiver.DownloadNotificationClickReceiver
import onlymash.flexbooru.util.IOUtils
import onlymash.flexbooru.util.fileName
import onlymash.flexbooru.okhttp.OkHttp3Downloader
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

        internal fun downloadPost(post: PostBase?, activity: Activity) {
            if (post == null) return
            val host = post.host
            val id = post.getPostId()
            val url = when (Settings.downloadSize) {
                Settings.POST_SIZE_SAMPLE -> post.getSampleUrl()
                Settings.POST_SIZE_LARGER -> post.getLargerUrl()
                else -> post.getOriginUrl()
            }
            if (url.isEmpty()) return
            var fileName = url.fileName()
            if (!fileName.contains(' ')) fileName = "$id - $fileName"
            val docUri = activity.getDownloadUri(host, fileName) ?: return
            val docId = DocumentsContract.getDocumentId(docUri)
            val workManager = WorkManager.getInstance(App.app)
            workManager.enqueue(
                OneTimeWorkRequestBuilder<DownloadWorker>()
                    .setInputData(
                        workDataOf(
                            URL_KEY to url,
                            HOST_KEY to host,
                            POST_ID_KEY to id,
                            TYPE_KEY to TYPE_POST,
                            DOC_ID_KEY to docId
                        )
                    )
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .build()
            )
        }

        internal fun download(url: String, postId: Int, host: String, activity: Activity) {
            if (url.isEmpty()) return
            var fileName = url.fileName()
            if (!fileName.contains(' ')) fileName = "$postId - $fileName"
            val docUri = activity.getDownloadUri(host, fileName) ?: return
            val docId = DocumentsContract.getDocumentId(docUri)
            val workManager = WorkManager.getInstance(App.app)
            workManager.enqueue(
                OneTimeWorkRequestBuilder<DownloadWorker>()
                    .setInputData(
                        workDataOf(
                            URL_KEY to url,
                            HOST_KEY to host,
                            POST_ID_KEY to postId,
                            TYPE_KEY to TYPE_POST,
                            DOC_ID_KEY to docId
                        )
                    )
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .build()
            )
        }

        internal fun downloadPool(
            activity: Activity,
            pool: PoolMoe,
            type: Int,
            username: String,
            passwordHash: String
        ) {

            val scheme = pool.scheme
            val host = pool.host
            val id = pool.id
            val fileName = when (type) {
                POOL_DOWNLOAD_TYPE_JPGS -> {
                    "$host - $id.jpg.zip"
                }
                else -> {
                    "$host - $id.png.zip"
                }
            }
            val docUri = activity.getPoolUri(fileName) ?: return
            val docId = DocumentsContract.getDocumentId(docUri)
            val workManager = WorkManager.getInstance(App.app)
            workManager.enqueue(
                OneTimeWorkRequestBuilder<DownloadWorker>()
                    .setInputData(
                        workDataOf(
                            SCHEME_KEY to scheme,
                            HOST_KEY to host,
                            POOL_ID_KEY to id,
                            POOL_DOWNLOAD_TYPE_KEY to type,
                            TYPE_KEY to TYPE_POOL,
                            USERNAME_KEY to username,
                            PASSWORD_HASH_KEY to passwordHash,
                            DOC_ID_KEY to docId
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

    override suspend fun doWork(): Result {
        when (inputData.getString(TYPE_KEY)) {
            TYPE_POST -> {
                val url = inputData.getString(URL_KEY)
                val id = inputData.getInt(POST_ID_KEY, -1)
                val host = inputData.getString(HOST_KEY)
                val docId =  inputData.getString(DOC_ID_KEY)
                if (url == null || id < 0 || host == null || docId == null) return Result.failure()
                val desUri = getFileUriByDocId(docId) ?: return Result.failure()
                val channelId = applicationContext.packageName + ".download"
                val notificationManager = getNotificationManager(
                    channelId,
                    applicationContext.getString(R.string.post_download))
                val title = "$host - $id"
                val downloadingNotificationBuilder = getDownloadingNotificationBuilder(title = title, url = url, channelId = channelId)
                var startTime = 0L
                var elapsedTime = 500L
                ProgressInterceptor.addListener(url, object :
                    ProgressListener {
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
                notificationManager.notify(id, getDownloadedNotificationBuilder(title = title, channelId = channelId, desUri = desUri).build())
                return Result.success()
            }
            TYPE_POOL -> {
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
                val desUri = getFileUriByDocId(docId) ?: return Result.failure()
                val channelId = applicationContext.packageName + ".download_pool"
                val notificationManager = getNotificationManager(
                    channelId,
                    applicationContext.getString(R.string.pool_download))
                val title = "$host - pool: $id"
                val downloadingNotificationBuilder = getDownloadingNotificationBuilder(title = title, url = url, channelId = channelId)
                var startTime = 0L
                var elapsedTime = 500L
                ProgressInterceptor.addListener(
                    url,
                    object : ProgressListener {
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
                var `is`: InputStream? = null
                val os = applicationContext.contentResolver.openOutputStream(desUri)
                try {
                    `is` = OkHttp3Downloader(applicationContext)
                        .load(url)
                        .body?.source()?.inputStream()
                    IOUtils.copy(`is`, os)
                } catch (_: IOException) {
                    return Result.failure()
                } finally {
                    ProgressInterceptor.removeListener(url)
                    IOUtils.closeQuietly(`is`)
                    IOUtils.closeQuietly(os)
                }
                notificationManager.notify(
                    id,
                    getDownloadedNotificationBuilder(
                        title = title,
                        channelId = channelId,
                        desUri = desUri
                    ).build()
                )
                return Result.success()
            }
        }
        return Result.failure()
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

    private fun getDownloadedNotificationBuilder(title: String, channelId: String, desUri: Uri): NotificationCompat.Builder {
        val intent = Intent(applicationContext, DownloadNotificationClickReceiver::class.java)
        intent.data = desUri
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