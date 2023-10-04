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

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.work.*
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderContract
import onlymash.flexbooru.app.App
import onlymash.flexbooru.app.Settings.activeMuzeiUid
import onlymash.flexbooru.data.database.MuzeiManager
import onlymash.flexbooru.data.database.dao.PostDao
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Settings.POST_SIZE_LARGER
import onlymash.flexbooru.app.Settings.POST_SIZE_SAMPLE
import onlymash.flexbooru.app.Settings.muzeiLimit
import onlymash.flexbooru.app.Settings.muzeiSize
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL_LEGACY
import onlymash.flexbooru.app.Values.BOORU_TYPE_SHIMMIE
import onlymash.flexbooru.data.database.BooruManager
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.extension.isStillImage
import org.koin.android.ext.android.inject


class MuzeiArtWorker(
    context: Context,
    workerParameters: WorkerParameters
) : Worker(context, workerParameters) {
    companion object {
        internal fun enqueueLoad() {
            val workManager = WorkManager.getInstance(App.app)
            workManager.enqueue(OneTimeWorkRequestBuilder<MuzeiArtWorker>()
                .setConstraints(
                    Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build())
                .build())
        }
    }
    override fun doWork(): Result {
        val uid = activeMuzeiUid
        val muzei = MuzeiManager.getMuzeiByUid(uid) ?: return Result.failure()
        val booru = BooruManager.getBooruByUid(muzei.booruUid) ?: return Result.failure()
        val postDao by App.app.inject<PostDao>()
        val posts = postDao.getPostsRaw(booruUid = muzei.booruUid, query = muzei.query, limit = muzeiLimit).filter { it.origin.isStillImage() }
        val providerClient = ProviderContract.getProviderClient(
            applicationContext, applicationContext.packageName + ".muzei")
        val attributionString = applicationContext.getString(R.string.muzei_attribution)
        val artworks = posts.map { post ->
            Artwork(
                token = "id:${post.id}",
                title = "Post ${post.id}",
                byline = post.query,
                attribution = attributionString,
                persistentUri = when (muzeiSize) {
                    POST_SIZE_SAMPLE -> post.sample.toUri()
                    POST_SIZE_LARGER -> post.medium.toUri()
                    else -> post.origin.toUri()
                },
                webUri = getWebUri(booru, post.id)
            )
        }
        providerClient.setArtwork(artworks)
        return Result.success()
    }

    private fun getWebUri(booru: Booru, postId: Int): Uri {
        return when (booru.type) {
            BOORU_TYPE_DAN -> String.format("%s://%s/posts/%d", booru.scheme, booru.host, postId).toUri()
            in arrayOf(BOORU_TYPE_GEL, BOORU_TYPE_GEL_LEGACY) -> String.format("%s://%s/index.php?page=post&s=view&id=%d", booru.scheme, booru.host, postId).toUri()
            BOORU_TYPE_SHIMMIE -> {
                if (booru.path.isNullOrBlank()) {
                    String.format("%s://%s/post/view/%d", booru.scheme, booru.host, postId).toUri()
                } else {
                    String.format("%s://%s/%s/post/view/%d", booru.scheme, booru.host, booru.path, postId).toUri()
                }
            }
            else -> String.format("%s://%s/post/show/%d",
                booru.scheme,
                if (booru.host.startsWith("capi-v2."))
                    booru.host.replaceFirst("capi-v2.", "chan.")
                else
                    booru.host,
                postId
            ).toUri()
        }
    }
}