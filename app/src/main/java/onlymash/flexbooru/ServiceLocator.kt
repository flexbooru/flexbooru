package onlymash.flexbooru

import android.app.Application
import androidx.annotation.VisibleForTesting
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.repository.post.PostData
import onlymash.flexbooru.repository.post.PostRepository
import java.util.concurrent.Executor
import java.util.concurrent.Executors

interface ServiceLocator {
    companion object {
        private val LOCK = Any()
        private var instance: ServiceLocator? = null
        fun instance(app: Application): ServiceLocator {
            synchronized(LOCK) {
                if (instance == null) {
                    instance = DefaultServiceLocator(app = app)
                }
                return instance!!
            }
        }

        @VisibleForTesting
        fun swap(locator: ServiceLocator) {
            instance = locator
        }
    }

    fun getPostRepository(): PostRepository

    fun getNetworkExecutor(): Executor

    fun getDiskIOExecutor(): Executor

    fun getDanbooruApi(): DanbooruApi

    fun getMoebooruApi(): MoebooruApi
}

/**
 * default implementation of ServiceLocator that uses production endpoints.
 */
open class DefaultServiceLocator(val app: Application) : ServiceLocator {

    // thread pool used for disk access
    @Suppress("PrivatePropertyName")
    private val DISK_IO = Executors.newSingleThreadExecutor()

    // thread pool used for network requests
    @Suppress("PrivatePropertyName")
    private val NETWORK_IO = Executors.newFixedThreadPool(5)

    private val db by lazy { FlexbooruDatabase.create(app) }

    private val danApi by lazy { DanbooruApi.create() }

    private val moeApi by lazy { MoebooruApi.create() }

    override fun getPostRepository(): PostRepository {
        return PostData(
            db = db,
            danbooruApi = getDanbooruApi(),
            moebooruApi = getMoebooruApi(),
            ioExecutor = getDiskIOExecutor()
        )
    }

    override fun getNetworkExecutor(): Executor = NETWORK_IO

    override fun getDiskIOExecutor(): Executor = DISK_IO

    override fun getDanbooruApi(): DanbooruApi = danApi

    override fun getMoebooruApi(): MoebooruApi = moeApi

}