package onlymash.flexbooru.repository.browse

import android.os.Handler
import onlymash.flexbooru.database.FlexbooruDatabase
import java.util.concurrent.Executor

class PostLoader(private val db: FlexbooruDatabase,
                 private val ioExecutor: Executor) {

    private var postLoadedListener: PostLoadedListener? = null

    fun setPostLoadedListener(listener: PostLoadedListener?) {
        postLoadedListener = listener
    }

    private var uiHandler: Handler? = null

    fun setUIHandler(handler: Handler) {
        uiHandler = handler
    }

    fun loadDanPosts(host: String, keyword: String) {
        ioExecutor.execute {
            val posts = db.postDanDao().getPostsRaw(host, keyword)
            uiHandler?.post {
                postLoadedListener?.onDanItemsLoaded(posts)
            }
        }
    }

    fun loadMoePosts(host: String, keyword: String) {
        ioExecutor.execute {
            val posts = db.postMoeDao().getPostsRaw(host, keyword)
            uiHandler?.post {
                postLoadedListener?.onMoeItemsLoaded(posts)
            }
        }
    }
}