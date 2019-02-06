package onlymash.flexbooru.repository.browse

import onlymash.flexbooru.database.FlexbooruDatabase
import java.util.concurrent.Executor

class PostLoader(private val db: FlexbooruDatabase,
                 private val ioExecutor: Executor) {

    private var postLoadedListener: PostLoadedListener? = null

    fun setPostLoadedListener(listener: PostLoadedListener?) {
        postLoadedListener = listener
    }

    fun loadDanPosts(host: String, keyword: String) {
        ioExecutor.execute {
            val posts = db.postDanDao().getPostsRaw(host, keyword)
            postLoadedListener?.onDanItemsLoaded(posts)
        }
    }

    fun loadMoePosts(host: String, keyword: String) {
        ioExecutor.execute {
            val posts = db.postMoeDao().getPostsRaw(host, keyword)
            postLoadedListener?.onMoeItemsLoaded(posts)
        }
    }
}