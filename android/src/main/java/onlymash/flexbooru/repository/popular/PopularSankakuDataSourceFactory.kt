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

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import kotlinx.coroutines.CoroutineScope
import onlymash.flexbooru.api.SankakuApi
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.entity.post.SearchPopular
import onlymash.flexbooru.entity.post.PostSankaku

/**
 * Sankaku popular posts data source factory which also provides a way to observe the last created data source.
 * This allows us to channel its network request status etc back to the UI. See the Listing creation
 * in the Repository class.
 */
class PopularSankakuDataSourceFactory(
    private val scope: CoroutineScope,
    private val sankakuApi: SankakuApi,
    private val db: FlexbooruDatabase,
    private val popular: SearchPopular) : DataSource.Factory<Int, PostSankaku>(){
    //source livedata
    val sourceLiveData = MutableLiveData<PopularSankakuDataSource>()
    override fun create(): DataSource<Int, PostSankaku> {
        val source = PopularSankakuDataSource(scope, sankakuApi, db, popular)
        sourceLiveData.postValue(source)
        return source
    }
}