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
import onlymash.flexbooru.api.SankakuApi
import onlymash.flexbooru.entity.tag.SearchTag
import onlymash.flexbooru.entity.tag.TagSankaku
import java.util.concurrent.Executor

class TagSankakuDataSourceFactory(
    private val sankakuApi: SankakuApi,
    private val search: SearchTag,
    private val retryExecutor: Executor
) : DataSource.Factory<Int, TagSankaku>() {
    //source livedata
    val sourceLiveData = MutableLiveData<TagSankakuDataSource>()
    override fun create(): DataSource<Int, TagSankaku> {
        val source = TagSankakuDataSource(sankakuApi, search, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}