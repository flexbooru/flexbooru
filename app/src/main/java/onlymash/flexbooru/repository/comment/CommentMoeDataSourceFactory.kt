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

package onlymash.flexbooru.repository.comment

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.entity.CommentAction
import onlymash.flexbooru.entity.CommentMoe
import java.util.concurrent.Executor

//Moebooru comment data source factory
class CommentMoeDataSourceFactory(
    private val moebooruApi: MoebooruApi,
    private val commentAction: CommentAction,
    private val retryExecutor: Executor
) : DataSource.Factory<Int, CommentMoe>() {
    //source livedata
    val sourceLiveData = MutableLiveData<CommentMoeDataSource>()
    override fun create(): DataSource<Int, CommentMoe> {
        val source = CommentMoeDataSource(moebooruApi, commentAction, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}