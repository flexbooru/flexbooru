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
import onlymash.flexbooru.api.*
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.repository.account.UserFinder
import onlymash.flexbooru.repository.account.UserRepository
import onlymash.flexbooru.repository.artist.ArtistData
import onlymash.flexbooru.repository.artist.ArtistRepository
import onlymash.flexbooru.repository.browse.PostLoader
import onlymash.flexbooru.repository.browse.PostLoaderRepository
import onlymash.flexbooru.repository.comment.CommentData
import onlymash.flexbooru.repository.comment.CommentRepository
import onlymash.flexbooru.repository.favorite.VoteData
import onlymash.flexbooru.repository.favorite.VoteRepository
import onlymash.flexbooru.repository.pool.PoolData
import onlymash.flexbooru.repository.pool.PoolRepository
import onlymash.flexbooru.repository.popular.PopularData
import onlymash.flexbooru.repository.popular.PopularRepository
import onlymash.flexbooru.repository.post.PostData
import onlymash.flexbooru.repository.post.PostRepository
import onlymash.flexbooru.repository.suggestion.SuggestionData
import onlymash.flexbooru.repository.suggestion.SuggestionRepository
import onlymash.flexbooru.repository.tag.TagData
import onlymash.flexbooru.repository.tag.TagRepository
import onlymash.flexbooru.repository.tagfilter.TagFilterDataSource
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Service locator implementation to allow us to replace default implementations
 *
 */
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
    fun getDanbooruOneApi(): DanbooruOneApi
    fun getDanbooruApi(): DanbooruApi
    fun getMoebooruApi(): MoebooruApi
    fun getGelbooruApi(): GelbooruApi
    fun getSankakuApi(): SankakuApi
    fun getPostLoader(): PostLoaderRepository
    fun getUserRepository(): UserRepository
    fun getTagFilterDataSource(): TagFilterDataSource
    fun getVoteRepository(): VoteRepository
    fun getCommentRepository(): CommentRepository
    fun getSuggestionRepository(): SuggestionRepository
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

    private val danOneApi by lazy { DanbooruOneApi.create() }

    private val danApi by lazy { DanbooruApi.create() }

    private val moeApi by lazy { MoebooruApi.create() }

    private val gelApi by lazy { GelbooruApi.create() }

    private val sanApi by lazy { SankakuApi.create() }

    override fun getPostRepository(): PostRepository {
        return PostData(
            db = FlexbooruDatabase.instance,
            danbooruOneApi = getDanbooruOneApi(),
            danbooruApi = getDanbooruApi(),
            moebooruApi = getMoebooruApi(),
            gelbooruApi = getGelbooruApi(),
            sankakuApi = getSankakuApi(),
            ioExecutor = getDiskIOExecutor()
        )
    }

    override fun getPopularRepository(): PopularRepository {
        return PopularData(
            danbooruApi = getDanbooruApi(),
            danbooruOneApi = getDanbooruOneApi(),
            moebooruApi = getMoebooruApi(),
            sankakuApi = getSankakuApi(),
            db = FlexbooruDatabase.instance,
            networkExecutor = getNetworkExecutor()
        )
    }

    override fun getPoolRepository(): PoolRepository {
        return PoolData(
            danbooruApi = getDanbooruApi(),
            danbooruOneApi = getDanbooruOneApi(),
            moebooruApi = getMoebooruApi(),
            sankakuApi = getSankakuApi(),
            networkExecutor = getNetworkExecutor()
        )
    }

    override fun getTagRepository(): TagRepository {
        return TagData(
            danbooruApi = getDanbooruApi(),
            danbooruOneApi = getDanbooruOneApi(),
            moebooruApi = getMoebooruApi(),
            gelbooruApi = getGelbooruApi(),
            sankakuApi = getSankakuApi(),
            networkExecutor = getNetworkExecutor()
        )
    }

    override fun getArtistRepository(): ArtistRepository {
        return ArtistData(
            danbooruApi = getDanbooruApi(),
            danbooruOneApi = getDanbooruOneApi(),
            moebooruApi = getMoebooruApi(),
            networkExecutor = getNetworkExecutor()
        )
    }

    override fun getVoteRepository(): VoteRepository {
        return VoteData(
            danbooruApi = getDanbooruApi(),
            danbooruOneApi = getDanbooruOneApi(),
            moebooruApi = getMoebooruApi(),
            sankakuApi = getSankakuApi(),
            db = FlexbooruDatabase.instance,
            ioExecutor = getDiskIOExecutor()
        )
    }

    override fun getCommentRepository(): CommentRepository {
        return CommentData(
            danbooruApi = getDanbooruApi(),
            danbooruOneApi = getDanbooruOneApi(),
            moebooruApi = getMoebooruApi(),
            gelbooruApi = getGelbooruApi(),
            sankakuApi = getSankakuApi(),
            networkExecutor = NETWORK_IO
        )
    }

    override fun getTagFilterDataSource(): TagFilterDataSource =
        TagFilterDataSource(FlexbooruDatabase.tagFilterDao)

    override fun getNetworkExecutor(): Executor = NETWORK_IO

    override fun getDiskIOExecutor(): Executor = DISK_IO

    override fun getDanbooruOneApi(): DanbooruOneApi = danOneApi

    override fun getDanbooruApi(): DanbooruApi = danApi

    override fun getMoebooruApi(): MoebooruApi = moeApi

    override fun getGelbooruApi(): GelbooruApi = gelApi

    override fun getSankakuApi(): SankakuApi = sanApi

    override fun getPostLoader(): PostLoaderRepository {
        return PostLoader(
            db = FlexbooruDatabase.instance,
            ioExecutor = getDiskIOExecutor()
        )
    }

    override fun getUserRepository(): UserRepository {
        return UserFinder(
            danbooruApi = getDanbooruApi(),
            danbooruOneApi = getDanbooruOneApi(),
            moebooruApi = getMoebooruApi(),
            sankakuApi = getSankakuApi()
        )
    }

    override fun getSuggestionRepository(): SuggestionRepository {
        return SuggestionData(
            danbooruApi = getDanbooruApi(),
            moebooruApi = getMoebooruApi(),
            danbooruOneApi = getDanbooruOneApi(),
            gelbooruApi = getGelbooruApi(),
            sankakuApi = getSankakuApi()
        )
    }
}