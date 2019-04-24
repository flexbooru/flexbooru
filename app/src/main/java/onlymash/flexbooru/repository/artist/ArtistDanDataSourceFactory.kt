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

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.entity.artist.ArtistDan
import onlymash.flexbooru.entity.artist.SearchArtist
import java.util.concurrent.Executor

/**
 *Danbooru artists data source factory
 * */
class ArtistDanDataSourceFactory(
    private val danbooruApi: DanbooruApi,
    private val search: SearchArtist,
    private val retryExecutor: Executor) : DataSource.Factory<Int, ArtistDan>() {
    //source livedata
    val sourceLiveData = MutableLiveData<ArtistDanDataSource>()
    override fun create(): DataSource<Int, ArtistDan> {
        val source = ArtistDanDataSource(danbooruApi, search, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}