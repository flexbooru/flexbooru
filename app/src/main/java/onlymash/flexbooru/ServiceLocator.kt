/*
 * Copyright (C) 2019 by onlymash <im@fiepi.me>, All rights reserved
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

package onlymash.flexbooru

import androidx.annotation.VisibleForTesting
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.repository.account.UserFinder
import onlymash.flexbooru.repository.artist.ArtistData
import onlymash.flexbooru.repository.artist.ArtistRepository
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

    fun getArtistRepository(): ArtistRepository

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

    override fun getArtistRepository(): ArtistRepository {
        return ArtistData(
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
            moebooruApi = getMoebooruApi()
        )
    }
}