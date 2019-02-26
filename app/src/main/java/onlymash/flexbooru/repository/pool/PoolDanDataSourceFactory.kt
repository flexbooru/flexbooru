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

package onlymash.flexbooru.repository.pool

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.entity.PoolDan
import onlymash.flexbooru.entity.Search
import java.util.concurrent.Executor

//danbooru pools data source factory
class PoolDanDataSourceFactory(
    private val danbooruApi: DanbooruApi,
    private val search: Search,
    private val retryExecutor: Executor) : DataSource.Factory<Int, PoolDan>() {
    //source livedata
    val sourceLiveData = MutableLiveData<PoolDanDataSource>()
    override fun create(): DataSource<Int, PoolDan> {
        val source = PoolDanDataSource(danbooruApi, search, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}