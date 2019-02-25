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

package onlymash.flexbooru.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import onlymash.flexbooru.entity.CommentAction
import onlymash.flexbooru.repository.comment.CommentRepository

class CommentViewModel(private val repo: CommentRepository) : ViewModel() {
    private val commentAction = MutableLiveData<CommentAction>()
    private val moeRepoResult = map(commentAction) {
        repo.getMoeComments(it)
    }
    val commentsMoe = Transformations.switchMap(moeRepoResult) { it.pagedList }!!
    val networkStateMoe = Transformations.switchMap(moeRepoResult) { it.networkState }!!
    val refreshStateMoe = Transformations.switchMap(moeRepoResult) { it.refreshState }!!

    private val danRepoResult = map(commentAction) {
        repo.getDanComments(it)
    }
    val commentsDan = Transformations.switchMap(danRepoResult) { it.pagedList }!!
    val networkStateDan = Transformations.switchMap(danRepoResult) { it.networkState }!!
    val refreshStateDan = Transformations.switchMap(danRepoResult) { it.refreshState }!!

    fun show(action: CommentAction): Boolean {
        if (commentAction.value == action) {
            return false
        }
        commentAction.value = action
        return true
    }

    fun refreshMoe() {
        moeRepoResult.value?.refresh?.invoke()
    }

    fun retryMoe() {
        moeRepoResult?.value?.retry?.invoke()
    }

    fun refreshDan() {
        danRepoResult.value?.refresh?.invoke()
    }

    fun retryDan() {
        danRepoResult?.value?.retry?.invoke()
    }

    fun createMoeComment(commentAction: CommentAction) {
        repo.createMoeComment(commentAction)
    }

    fun deleteMoeComment(commentAction: CommentAction) {
        repo.destroyMoeComment(commentAction)
    }

    fun createDanComment(commentAction: CommentAction) {
        repo.createDanComment(commentAction)
    }

    fun deleteDanComment(commentAction: CommentAction) {
        repo.destroyDanComment(commentAction)
    }
}