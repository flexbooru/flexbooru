/*
 * Copyright (C) 2020. by onlymash <fiepi.dev@gmail.com>, All rights reserved
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
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import onlymash.flexbooru.data.database.dao.PostDao

class DetailViewModel(postDao: PostDao, booruUid: Long, query: String) : ScopeViewModel() {

    val posts = Pager(
        config = PagingConfig(
            pageSize = 30,
            enablePlaceholders = true
        )
    ) {
        postDao.getPosts(booruUid, query)
    }
        .flow
        .cachedIn(viewModelScope)

    private val _currentPosition = MutableLiveData<Int>()

    var currentPosition: Int
        get() = _currentPosition.value ?: -1
        set(value) {
            _currentPosition.value = value
        }
}