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

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import onlymash.flexbooru.entity.comment.CommentAction
import onlymash.flexbooru.repository.comment.CommentRepository
import onlymash.flexbooru.repository.comment.CommentState

class CommentViewModel(private val repo: CommentRepository) : ViewModel() {
    private val commentAction = MutableLiveData<CommentAction>()
    private val moeRepoResult = map(commentAction) {
        repo.getMoeComments(it)
    }
    val commentsMoe = Transformations.switchMap(moeRepoResult) { it.pagedList }
    val networkStateMoe = Transformations.switchMap(moeRepoResult) { it.networkState }
    val refreshStateMoe = Transformations.switchMap(moeRepoResult) { it.refreshState }

    private val danRepoResult = map(commentAction) {
        repo.getDanComments(it)
    }
    val commentsDan = Transformations.switchMap(danRepoResult) { it.pagedList }
    val networkStateDan = Transformations.switchMap(danRepoResult) { it.networkState }
    val refreshStateDan = Transformations.switchMap(danRepoResult) { it.refreshState }

    private val danOneRepoResult = map(commentAction) {
        repo.getDanOneComments(it)
    }
    val commentsDanOne = Transformations.switchMap(danOneRepoResult) { it.pagedList }
    val networkStateDanOne = Transformations.switchMap(danOneRepoResult) { it.networkState }
    val refreshStateDanOne = Transformations.switchMap(danOneRepoResult) { it.refreshState }

    private val gelRepoResult = map(commentAction) {
        repo.getGelComments(it)
    }
    val commentsGel = Transformations.switchMap(gelRepoResult) { it.pagedList }
    val networkStateGel = Transformations.switchMap(gelRepoResult) { it.networkState }
    val refreshStateGel = Transformations.switchMap(gelRepoResult) { it.refreshState }

    private val sankakuRepoResult = map(commentAction) {
        repo.getSankakuComments(it)
    }
    val commentsSankaku = Transformations.switchMap(sankakuRepoResult) { it.pagedList }
    val networkStateSankaku = Transformations.switchMap(sankakuRepoResult) { it.networkState }
    val refreshStateSankaku = Transformations.switchMap(sankakuRepoResult) { it.refreshState }

    val commentState = MediatorLiveData<CommentState>().apply {
        addSource(repo.commentState) {
            postValue(it)
        }
    }

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
        moeRepoResult.value?.retry?.invoke()
    }

    fun refreshDan() {
        danRepoResult.value?.refresh?.invoke()
    }

    fun retryDan() {
        danRepoResult.value?.retry?.invoke()
    }

    fun refreshDanOne() {
        danOneRepoResult.value?.refresh?.invoke()
    }

    fun retryDanOne() {
        danOneRepoResult.value?.retry?.invoke()
    }

    fun refreshGel() {
        gelRepoResult.value?.refresh?.invoke()
    }

    fun retryGel() {
        gelRepoResult.value?.retry?.invoke()
    }

    fun refreshSankaku() {
        sankakuRepoResult.value?.refresh?.invoke()
    }

    fun retrySankaku() {
        sankakuRepoResult.value?.retry?.invoke()
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

    fun createDanOneComment(commentAction: CommentAction) {
        repo.createDanOneComment(commentAction)
    }

    fun deleteDanOneComment(commentAction: CommentAction) {
        repo.destroyDanOneComment(commentAction)
    }

    fun createSankakuComment(commentAction: CommentAction) {
        repo.createSankakuComment(commentAction)
    }

    fun deleteSankakuComment(commentAction: CommentAction) {
        repo.destroySankakuComment(commentAction)
    }
}