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

package onlymash.flexbooru.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import onlymash.flexbooru.data.action.ActionComment
import onlymash.flexbooru.data.repository.comment.CommentRepository
import onlymash.flexbooru.extension.NetResult

class CommentViewModel(private val repository: CommentRepository) : ScopeViewModel() {

    private val _action: MutableLiveData<ActionComment> = MutableLiveData()

    private val _result = Transformations.map(_action) {
        repository.getComments(viewModelScope, it)
    }

    val comments = Transformations.switchMap(_result) { it.pagedList }

    val networkState = Transformations.switchMap(_result) { it.networkState }

    val refreshState = Transformations.switchMap(_result) { it.refreshState }

    val commentState: MutableLiveData<NetResult<Boolean>> = MutableLiveData()

    fun show(action: ActionComment?): Boolean {
        if (_action.value == action) {
            return false
        }
        _action.value = action
        return true
    }

    fun createCommment(action: ActionComment) {
        viewModelScope.launch {
            commentState.postValue(repository.createComment(action))
        }
    }

    fun deleteComment(action: ActionComment) {
        viewModelScope.launch {
            commentState.postValue(repository.destroyComment(action))
        }
    }

    fun refresh() {
        _result.value?.refresh?.invoke()
    }

    fun retry() {
        _result.value?.retry?.invoke()
    }
}