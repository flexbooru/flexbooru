package onlymash.flexbooru

import androidx.annotation.VisibleForTesting
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.repository.account.UserFinder
import onlymash.flexbooru.repository.browse.PostLoader
import onlymash.flexbooru.repository.pool.PoolData
import onlymash.flexbooru.repository.pool.PoolRepository
import onlymash.flexbooru.repository.popular.PopularData
import onlymash.flexbooru.repository.popular.PopularRepository
import onlymash.flexbooru.repository.post.PostData
import onlymash.flexbooru.repository.post.PostRepository
import onlymash.flexbooru.repository.tag.TagData
import onlymash.flexbooru.repository.tag.TagRepository
import java.util.concurrent.Executor
import java.util.concurrent.Executors

interface ServiceLocator {
    companion object {
        private val LOCK = Any()
        private var instance: ServiceLocator? = null
        fun instance(): ServiceLocator {
            synchronized(LOCK) {
                if (instance == null) {
                    instance = DefaultServiceLocator()
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

    fun getPopularRepository(): PopularRepository

    fun getPoolRepository(): PoolRepository

    fun getTagRepository(): TagRepository

    fun getNetworkExecutor(): Executor

    fun getDiskIOExecutor(): Executor

    fun getDanbooruApi(): DanbooruApi

    fun getMoebooruApi(): MoebooruApi

    fun getPostLoader(): PostLoader

    fun getUserFinder(): UserFinder
}

/**
 * default implementation of ServiceLocator that uses production endpoints.
 */
open class DefaultServiceLocator : ServiceLocator {

    // thread pool used for disk access
    @Suppress("PrivatePropertyName")
    private val DISK_IO = Executors.newSingleThreadExecutor()

    // thread pool used for network requests
    @Suppress("PrivatePropertyName")
    private val NETWORK_IO = Executors.newFixedThreadPool(5)

    private val danApi by lazy { DanbooruApi.create() }

    private val moeApi by lazy { MoebooruApi.create() }

    override fun getPostRepository(): PostRepository {
        return PostData(
            db = FlexbooruDatabase.instance,
            danbooruApi = getDanbooruApi(),
            moebooruApi = getMoebooruApi(),
            ioExecutor = getDiskIOExecutor()
        )
    }

    override fun getPopularRepository(): PopularRepository {
        return PopularData(
            danbooruApi = getDanbooruApi(),
            moebooruApi = getMoebooruApi(),
            db = FlexbooruDatabase.instance,
            networkExecutor = getNetworkExecutor()
        )
    }

    override fun getPoolRepository(): PoolRepository {
        return PoolData(
            danbooruApi = getDanbooruApi(),
            moebooruApi = getMoebooruApi(),
            networkExecutor = getNetworkExecutor()
        )
    }

    override fun getTagRepository(): TagRepository {
        return TagData(
            danbooruApi = getDanbooruApi(),
            moebooruApi = getMoebooruApi(),
            networkExecutor = getNetworkExecutor()
        )
    }

    override fun getNetworkExecutor(): Executor = NETWORK_IO

    override fun getDiskIOExecutor(): Executor = DISK_IO

    override fun getDanbooruApi(): DanbooruApi = danApi

    override fun getMoebooruApi(): MoebooruApi = moeApi

    override fun getPostLoader(): PostLoader {
        return PostLoader(
            db = FlexbooruDatabase.instance,
            ioExecutor = getDiskIOExecutor()
        )
    }

    override fun getUserFinder(): UserFinder {
        return UserFinder(
            danbooruApi = getDanbooruApi(),
            moebooruApi = getMoebooruApi(),
            ioExecutor = DISK_IO
        )
    }
}