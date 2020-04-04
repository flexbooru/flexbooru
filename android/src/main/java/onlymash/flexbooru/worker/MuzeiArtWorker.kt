package onlymash.flexbooru.worker

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.work.*
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderContract
import onlymash.flexbooru.common.App
import onlymash.flexbooru.common.Settings.activeMuzeiUid
import onlymash.flexbooru.data.database.MuzeiManager
import onlymash.flexbooru.data.database.dao.PostDao
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings.POST_SIZE_LARGER
import onlymash.flexbooru.common.Settings.POST_SIZE_SAMPLE
import onlymash.flexbooru.common.Settings.muzeiLimit
import onlymash.flexbooru.common.Settings.muzeiSize
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.common.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.common.Values.BOORU_TYPE_SHIMMIE
import onlymash.flexbooru.data.database.BooruManager
import onlymash.flexbooru.data.model.common.Booru
import org.kodein.di.erased.instance


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
        val postDao by App.app.instance<PostDao>()
        val posts = postDao.getPostsRaw(booruUid = muzei.booruUid, query = muzei.query, limit = muzeiLimit)
        val providerClient = ProviderContract.getProviderClient(
            applicationContext, applicationContext.packageName + ".muzei")
        val attributionString = applicationContext.getString(R.string.muzei_attribution)
        val artworks = posts.map { post ->
            Artwork().apply {
                token = "id:${post.id}"
                title = "Post ${post.id}"
                byline = post.query
                attribution = attributionString
                persistentUri = when (muzeiSize) {
                    POST_SIZE_SAMPLE -> post.sample.toUri()
                    POST_SIZE_LARGER -> post.medium.toUri()
                    else -> post.origin.toUri()
                }
                webUri = getWebUri(booru, post.id)
            }
        }
        providerClient.setArtwork(artworks)
        return Result.success()
    }

    private fun getWebUri(booru: Booru, postId: Int): Uri {
        return when (booru.type) {
            BOORU_TYPE_DAN -> String.format("%s://%s/posts/%d", booru.scheme, booru.host, postId).toUri()
            BOORU_TYPE_GEL -> String.format("%s://%s/index.php?page=post&s=view&id=%d", booru.scheme, booru.host, postId).toUri()
            BOORU_TYPE_SHIMMIE -> {
                if (booru.path.isNullOrBlank()) {
                    String.format("%s://%s/post/view/%d", booru.scheme, booru.host, postId).toUri()
                } else {
                    String.format("%s://%s/%s/post/view/%d", booru.scheme, booru.host, booru.path, postId).toUri()
                }
            }
            else -> String.format("%s://%s/post/show/%d", booru.scheme, booru.host, postId).toUri()
        }
    }
}