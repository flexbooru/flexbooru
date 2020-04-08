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
import onlymash.flexbooru.data.action.ActionPost
import onlymash.flexbooru.data.repository.post.PostRepository

class PostViewModel(private val repository: PostRepository) : ScopeViewModel() {

    private val _action: MutableLiveData<ActionPost> = MutableLiveData()

    private val _result = Transformations.map(_action) {
        repository.getPosts(viewModelScope, it)
    }

    val posts = Transformations.switchMap(_result) { it.pagedList }

    val networkState = Transformations.switchMap(_result) { it.networkState }

    val refreshState = Transformations.switchMap(_result) { it.refreshState }

    fun show(action: ActionPost?): Boolean {
        if (_action.value == action) {
            return false
        }
        _action.value = action
        return true
    }

    fun refresh() {
        _result.value?.refresh?.invoke()
    }

    fun retry() {
        _result.value?.retry?.invoke()
    }
}