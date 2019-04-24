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

package onlymash.flexbooru.repository.pool

import androidx.annotation.MainThread
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.toLiveData
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.DanbooruOneApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.api.SankakuApi
import onlymash.flexbooru.entity.pool.PoolDan
import onlymash.flexbooru.entity.pool.PoolDanOne
import onlymash.flexbooru.entity.pool.PoolMoe
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.entity.pool.PoolSankaku
import onlymash.flexbooru.repository.Listing
import java.util.concurrent.Executor

//pools data source
class PoolData(private val danbooruApi: DanbooruApi,
               private val danbooruOneApi: DanbooruOneApi,
               private val moebooruApi: MoebooruApi,
               private val sankakuApi: SankakuApi,
               private val networkExecutor: Executor
) : PoolRepository {

    @MainThread
    override fun getDanPools(search: Search): Listing<PoolDan> {
        val sourceFactory = PoolDanDataSourceFactory(danbooruApi = danbooruApi, search = search, retryExecutor = networkExecutor)
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = search.limit,
                enablePlaceholders = true
            ),
            fetchExecutor = networkExecutor)
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) { poolDanDataSource ->
            poolDanDataSource.initialLoad
        }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { poolDanDataSource ->
                poolDanDataSource.networkState
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
    override fun getMoePools(search: Search): Listing<PoolMoe> {
        val sourceFactory = PoolMoeDataSourceFactory(moebooruApi = moebooruApi, search = search, retryExecutor = networkExecutor)
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = 20,
                enablePlaceholders = true
            ),
            fetchExecutor = networkExecutor)
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) { poolMoeDataSource ->
            poolMoeDataSource.initialLoad
        }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { poolMoeDataSource ->
                poolMoeDataSource.networkState
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
    override fun getDanOnePools(search: Search): Listing<PoolDanOne> {
        val sourceFactory = PoolDanOneDataSourceFactory(danbooruOneApi = danbooruOneApi, search = search, retryExecutor = networkExecutor)
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = PoolDanOneDataSource.PAGE_SIZE,
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
    override fun getSankakuPools(search: Search): Listing<PoolSankaku> {
        val sourceFactory = PoolSankakuDataSourceFactory(sankakuApi = sankakuApi, search = search, retryExecutor = networkExecutor)
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = search.limit,
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