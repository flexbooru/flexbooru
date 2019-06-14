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

package onlymash.flexbooru.repository.popular

import androidx.annotation.MainThread
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.toLiveData
import kotlinx.coroutines.CoroutineScope
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.DanbooruOneApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.api.SankakuApi
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.entity.post.*
import onlymash.flexbooru.repository.Listing
import java.util.concurrent.Executor

//popular posts data source
class PopularRepositoryImpl(
    private val danbooruApi: DanbooruApi,
    private val danbooruOneApi: DanbooruOneApi,
    private val moebooruApi: MoebooruApi,
    private val sankakuApi: SankakuApi,
    private val db: FlexbooruDatabase,
    private val networkExecutor: Executor) : PopularRepository {


    @MainThread
    override fun getDanPopular(scope: CoroutineScope, popular: SearchPopular): Listing<PostDan> {
        val sourceFactory = PopularDanDataSourceFactory(scope, danbooruApi, db, popular)
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = 20,
                enablePlaceholders = true
            ),
            fetchExecutor = networkExecutor)
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) { popularMoeDataSource ->
            popularMoeDataSource.initialLoad
        }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { popularDanDataSource ->
                popularDanDataSource.networkState
            },
            retry = {
                sourceFactory.sourceLiveData.value?.retryAllFailed()
            },
            refresh = {
                sourceFactory.sourceLiveData.value?.invalidate()
            },
            refreshState = refreshState
        )
    }

    @MainThread
    override fun getMoePopular(scope: CoroutineScope, popular: SearchPopular): Listing<PostMoe> {
        val sourceFactory = PopularMoeDataSourceFactory(scope, moebooruApi, db, popular)
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = 40,
                enablePlaceholders = true
            ),
            fetchExecutor = networkExecutor)
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) { popularMoeDataSource ->
            popularMoeDataSource.initialLoad
        }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { popularMoeDataSource ->
                popularMoeDataSource.networkState
            },
            retry = {
                sourceFactory.sourceLiveData.value?.retryAllFailed()
            },
            refresh = {
                sourceFactory.sourceLiveData.value?.invalidate()
            },
            refreshState = refreshState
        )
    }

    @MainThread
    override fun getDanOnePopular(scope: CoroutineScope, popular: SearchPopular): Listing<PostDanOne> {
        val sourceFactory = PopularDanOneDataSourceFactory(scope, danbooruOneApi, db, popular)
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = 20,
                enablePlaceholders = true
            ),
            fetchExecutor = networkExecutor)
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) { it.initialLoad }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { it.networkState },
            retry = { sourceFactory.sourceLiveData.value?.retryAllFailed() },
            refresh = { sourceFactory.sourceLiveData.value?.invalidate() },
            refreshState = refreshState
        )
    }

    @MainThread
    override fun getSankakuPopular(scope: CoroutineScope, popular: SearchPopular): Listing<PostSankaku> {
        val sourceFactory = PopularSankakuDataSourceFactory(scope, sankakuApi, db, popular)
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = 30,
                enablePlaceholders = true
            ),
            fetchExecutor = networkExecutor)
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) { it.initialLoad }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { it.networkState },
            retry = { sourceFactory.sourceLiveData.value?.retryAllFailed() },
            refresh = { sourceFactory.sourceLiveData.value?.invalidate() },
            refreshState = refreshState
        )
    }
}