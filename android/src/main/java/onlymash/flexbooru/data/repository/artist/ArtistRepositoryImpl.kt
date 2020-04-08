/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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

package onlymash.flexbooru.data.repository.artist

import androidx.annotation.MainThread
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.toLiveData
import kotlinx.coroutines.CoroutineScope
import onlymash.flexbooru.data.action.ActionArtist
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.model.common.Artist
import onlymash.flexbooru.data.repository.Listing

class ArtistRepositoryImpl(private val booruApis: BooruApis) : ArtistRepository {

    @MainThread
    override fun getArtists(scope: CoroutineScope, action: ActionArtist): Listing<Artist> {

        val sourceFactory = ArtistDataSourceFactory(action, booruApis, scope)

        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = action.limit,
                enablePlaceholders = true
            )
        )

        val refreshState = Transformations
            .switchMap(sourceFactory.sourceLiveData) { it.initialLoad }

        return Listing(
            pagedList = livePagedList,
            networkState = Transformations
                .switchMap(sourceFactory.sourceLiveData) { it.networkState },
            retry = {
                sourceFactory.sourceLiveData.value?.retryAllFailed()
            },
            refresh = {
                sourceFactory.sourceLiveData.value?.invalidate()
            },
            refreshState = refreshState
        )
    }
}