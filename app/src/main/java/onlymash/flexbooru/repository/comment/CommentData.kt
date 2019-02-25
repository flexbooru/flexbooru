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

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.toLiveData
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.entity.CommentAction
import onlymash.flexbooru.entity.CommentDan
import onlymash.flexbooru.entity.CommentMoe
import onlymash.flexbooru.repository.Listing
import java.util.concurrent.Executor

class CommentData(private val danbooruApi: DanbooruApi,
                  private val moebooruApi: MoebooruApi,
                  private val networkExecutor: Executor
) : CommentRepository {
    companion object {
        private const val TAG = "CommentData"
    }

    override fun getDanComments(commentAction: CommentAction): Listing<CommentDan> {
        val sourceFactory = CommentDanDataSourceFactory(
            danbooruApi = danbooruApi,
            commentAction = commentAction,
            retryExecutor = networkExecutor
        )
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = commentAction.limit,
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

    override fun createDanComment(commentAction: CommentAction) {

    }

    override fun destroyDanComment(commentAction: CommentAction) {

    }

    @MainThread
    override fun getMoeComments(commentAction: CommentAction): Listing<CommentMoe> {
        val sourceFactory = CommentMoeDataSourceFactory(
            moebooruApi = moebooruApi,
            commentAction = commentAction,
            retryExecutor = networkExecutor)
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = 30,
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

    @MainThread
    override fun createMoeComment(commentAction: CommentAction) {

    }

    @MainThread
    override fun destroyMoeComment(commentAction: CommentAction) {

    }

    override fun onSuccess() {
        Log.i(TAG, "onSuccess")
    }

    override fun onFailed(msg: String) {
        Log.e(TAG, "onFailed. $msg")
    }
}