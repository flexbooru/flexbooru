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

package onlymash.flexbooru.repository.tag

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.entity.tag.SearchTag
import onlymash.flexbooru.entity.tag.TagMoe
import java.util.concurrent.Executor

/**
 * Moebooru tags data source factory which also provides a way to observe the last created data source.
 * This allows us to channel its network request status etc back to the UI. See the Listing creation
 * in the Repository class.
 */
class TagMoeDataSourceFactory(
    private val moebooruApi: MoebooruApi,
    private val search: SearchTag,
    private val retryExecutor: Executor) : DataSource.Factory<Int, TagMoe>() {
    //source livedata
    val sourceLiveData = MutableLiveData<TagMoeDataSource>()
    override fun create(): DataSource<Int, TagMoe> {
        val source = TagMoeDataSource(moebooruApi, search, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}