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

import android.content.Context
import androidx.core.net.toUri
import androidx.work.*
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderContract
import onlymash.flexbooru.R
import onlymash.flexbooru.api.*
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.database.UserManager
import onlymash.flexbooru.entity.Search
import java.io.IOException
import onlymash.flexbooru.api.url.*
import onlymash.flexbooru.common.App
import onlymash.flexbooru.common.Constants
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.database.MuzeiManager
import org.kodein.di.generic.instance

class MuzeiArtWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {
    companion object {
        internal fun enqueueLoad() {
            val workManager = WorkManager.getInstance(App.app)
            workManager.enqueue(OneTimeWorkRequestBuilder<MuzeiArtWorker>()
                .setConstraints(Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build())
                .build())
        }
    }
    override suspend fun doWork(): Result {
        val uid = Settings.activeBooruUid
        val booru = BooruManager.getBooruByUid(uid) ?: return Result.failure()
        val user = UserManager.getUserByBooruUid(uid)
        val data = MuzeiManager.getMuzeiByBooruUid(uid)
        val muzeiUid = Settings.activeMuzeiUid
        var keyword = ""
        val muzeiSize = Settings.muzeiSize
        data?.let { list ->
            list.forEach { muzei ->
                if(muzei.uid == muzeiUid) {
                    keyword = muzei.keyword
                    return@forEach
                }
            }
            if (list.size > 0 && keyword.isEmpty()) {
                keyword = data[0].keyword
            }
        }
        when (booru.type) {
            Constants.TYPE_DANBOORU -> {
                val search = Search(
                    scheme = booru.scheme,
                    host = booru.host,
                    keyword = keyword,
                    limit = Settings.muzeiLimit).apply {
                    user?.let {
                        auth_key = it.api_key ?: ""
                        username = it.name
                    }
                }
                val danbooruApi: DanbooruApi by App.app.instance()
                val posts = try {
                    danbooruApi.getPosts(DanUrlHelper.getPostUrl(search, 1))
                        .execute().body() ?: throw IOException("Response was null")
                } catch (ex: IOException) {
                    return Result.retry()
                }
                if (posts.isEmpty()) {
                    return Result.failure()
                }
                val providerClient = ProviderContract.getProviderClient(
                    applicationContext, applicationContext.packageName + ".muzei")
                val attributionString = applicationContext.getString(R.string.muzei_attribution)
                providerClient.setArtwork(posts.map { post ->
                    Artwork().apply {
                        token = "id:${post.id}"
                        title = "Post ${post.id}"
                        byline = keyword
                        attribution = attributionString
                        persistentUri = when (muzeiSize) {
                            Settings.POST_SIZE_SAMPLE -> post.getSampleUrl().toUri()
                            Settings.POST_SIZE_LARGER -> post.getLargerUrl().toUri()
                            else -> post.getOriginUrl().toUri()
                        }
                        webUri = String.format("%s://%s/posts/%d", booru.scheme, booru.host, post.id).toUri()
                    }
                })
            }
            Constants.TYPE_MOEBOORU -> {
                val search = Search(
                    scheme = booru.scheme,
                    host = booru.host,
                    keyword = keyword,
                    limit = Settings.muzeiLimit).apply {
                    user?.let {
                        auth_key = it.password_hash ?: ""
                        username = it.name
                    }
                }
                val moebooruApi: MoebooruApi by App.app.instance()
                val posts = try {
                    moebooruApi.getPosts(MoeUrlHelper.getPostUrl(search, 1, search.keyword))
                        .execute().body() ?: throw IOException("Response was null")
                } catch (ex: IOException) {
                    return Result.retry()
                }
                val providerClient = ProviderContract.getProviderClient(
                    applicationContext, applicationContext.packageName + ".muzei")
                val attributionString = applicationContext.getString(R.string.muzei_attribution)
                providerClient.setArtwork(posts.map { post ->
                    Artwork().apply {
                        token = "id:${post.id}"
                        title = "Post ${post.id}"
                        byline = keyword
                        attribution = attributionString
                        persistentUri = when (muzeiSize) {
                            Settings.POST_SIZE_SAMPLE -> post.getSampleUrl().toUri()
                            Settings.POST_SIZE_LARGER -> post.getLargerUrl().toUri()
                            else -> post.getOriginUrl().toUri()
                        }
                        webUri = String.format("%s://%s/post/show/%d", booru.scheme, booru.host, post.id).toUri()
                    }
                })
            }
            Constants.TYPE_DANBOORU_ONE -> {
                val search = Search(
                    scheme = booru.scheme,
                    host = booru.host,
                    keyword = keyword,
                    limit = Settings.muzeiLimit).apply {
                    user?.let {
                        auth_key = it.password_hash ?: ""
                        username = it.name
                    }
                }
                val danbooruOneApi: DanbooruOneApi by App.app.instance()
                val posts = try {
                    danbooruOneApi.getPosts(DanOneUrlHelper.getPostUrl(search, 1))
                        .execute().body() ?: throw IOException("Response was null")
                } catch (ex: IOException) {
                    return Result.retry()
                }
                val providerClient = ProviderContract.getProviderClient(
                    applicationContext, applicationContext.packageName + ".muzei")
                val attributionString = applicationContext.getString(R.string.muzei_attribution)
                providerClient.setArtwork(posts.map { post ->
                    Artwork().apply {
                        token = "id:${post.id}"
                        title = "Post ${post.id}"
                        byline = keyword
                        attribution = attributionString
                        persistentUri = when (muzeiSize) {
                            Settings.POST_SIZE_SAMPLE -> post.getSampleUrl().toUri()
                            Settings.POST_SIZE_LARGER -> post.getLargerUrl().toUri()
                            else -> post.getOriginUrl().toUri()
                        }
                        webUri = String.format("%s://%s/post/show/%d", booru.scheme, booru.host, post.id).toUri()
                    }
                })
            }
            Constants.TYPE_GELBOORU -> {
                val search = Search(
                    scheme = booru.scheme,
                    host = booru.host,
                    keyword = keyword,
                    limit = Settings.muzeiLimit
                )
                val gelbooruApi: GelbooruApi by App.app.instance()
                val posts = try {
                    gelbooruApi.getPosts(GelUrlHelper.getPostUrl(search, 1))
                        .execute().body()?.posts ?: throw IOException("Response was null")
                } catch (ex: IOException) {
                    return Result.retry()
                }
                if (posts.isEmpty()) {
                    return Result.failure()
                }
                val providerClient = ProviderContract.getProviderClient(
                    applicationContext, applicationContext.packageName + ".muzei")
                val attributionString = applicationContext.getString(R.string.muzei_attribution)
                providerClient.setArtwork(posts.map { post ->
                    Artwork().apply {
                        token = "id:${post.id}"
                        title = "Post ${post.id}"
                        byline = keyword
                        attribution = attributionString
                        persistentUri = when (muzeiSize) {
                            Settings.POST_SIZE_SAMPLE -> post.getSampleUrl().toUri()
                            Settings.POST_SIZE_LARGER -> post.getLargerUrl().toUri()
                            else -> post.getOriginUrl().toUri()
                        }
                        webUri = String.format("%s://%s/index.php?page=post&s=view&id=%d", booru.scheme, booru.host, post.id).toUri()
                    }
                })
            }
            Constants.TYPE_SANKAKU -> {
                val search = Search(
                    scheme = booru.scheme,
                    host = booru.host,
                    keyword = keyword,
                    limit = Settings.muzeiLimit).apply {
                    user?.let {
                        auth_key = it.password_hash ?: ""
                        username = it.name
                    }
                }
                val sankakuApi: SankakuApi by App.app.instance()
                val posts = try {
                    sankakuApi.getPosts(SankakuUrlHelper.getPostUrl(search, 1))
                        .execute().body() ?: throw IOException("Response was null")
                } catch (ex: IOException) {
                    return Result.retry()
                }
                val providerClient = ProviderContract.getProviderClient(
                    applicationContext, applicationContext.packageName + ".muzei")
                val attributionString = applicationContext.getString(R.string.muzei_attribution)
                var host = booru.host
                if (host.startsWith("capi-v2.")) host = host.replaceFirst("capi-v2.", "beta.")
                providerClient.setArtwork(posts.map { post ->
                    Artwork().apply {
                        token = "id:${post.id}"
                        title = "Post ${post.id}"
                        byline = keyword
                        attribution = attributionString
                        persistentUri = when (muzeiSize) {
                            Settings.POST_SIZE_SAMPLE -> post.getSampleUrl().toUri()
                            Settings.POST_SIZE_LARGER -> post.getLargerUrl().toUri()
                            else -> post.getOriginUrl().toUri()
                        }
                        webUri = String.format("%s://%s/post/show/%d", booru.scheme, host, post.id).toUri()
                    }
                })
            }
        }
        return Result.success()
    }
}