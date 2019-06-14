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

package onlymash.flexbooru.repository.artist

import androidx.annotation.MainThread
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.toLiveData
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.DanbooruOneApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.entity.artist.ArtistDan
import onlymash.flexbooru.entity.artist.ArtistDanOne
import onlymash.flexbooru.entity.artist.ArtistMoe
import onlymash.flexbooru.entity.artist.SearchArtist
import onlymash.flexbooru.repository.Listing
import java.util.concurrent.Executor

/**
 *Artists data repository
 * */
class ArtistRepositoryImpl(private val danbooruApi: DanbooruApi,
                           private val danbooruOneApi: DanbooruOneApi,
                           private val moebooruApi: MoebooruApi,
                           private val networkExecutor: Executor
) : ArtistRepository {

    @MainThread
    override fun getDanArtists(search: SearchArtist): Listing<ArtistDan> {
        val sourceFactory = ArtistDanDataSourceFactory(danbooruApi = danbooruApi, search = search, retryExecutor = networkExecutor)
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = search.limit,
                enablePlaceholders = true
            ),
            fetchExecutor = networkExecutor)
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) { artistDanDataSource ->
            artistDanDataSource.initialLoad
        }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { artistDanDataSource ->
                artistDanDataSource.networkState
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
    override fun getMoeArtists(search: SearchArtist): Listing<ArtistMoe> {
        val sourceFactory = ArtistMoeDataSourceFactory(moebooruApi = moebooruApi, search = search, retryExecutor = networkExecutor)
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = 25,
                enablePlaceholders = true
            ),
            fetchExecutor = networkExecutor)
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) { artistMoeDataSource ->
            artistMoeDataSource.initialLoad
        }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { artistMoeDataSource ->
                artistMoeDataSource.networkState
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
    override fun getDanOneArtists(search: SearchArtist): Listing<ArtistDanOne> {
        val sourceFactory = ArtistDanOneDataSourceFactory(danbooruOneApi = danbooruOneApi , search = search, retryExecutor = networkExecutor)
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = 25,
                enablePlaceholders = true
            ),
            fetchExecutor = networkExecutor)
        val refreshState =
            Transformations.switchMap(sourceFactory.sourceLiveData) { it.initialLoad }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { it.networkState },
            retry = { sourceFactory.sourceLiveData.value?.retryAllFailed() },
            refresh = { sourceFactory.sourceLiveData.value?.invalidate() },
            refreshState = refreshState
        )
    }
}