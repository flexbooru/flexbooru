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

package onlymash.flexbooru.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import onlymash.flexbooru.database.dao.TagBlacklistDao
import onlymash.flexbooru.entity.TagBlacklist

class TagBlacklistViewModel(private val tagBlacklistDao: TagBlacklistDao) : ScopeViewModel() {

    private val tagOutcome: MediatorLiveData<MutableList<TagBlacklist>> = MediatorLiveData()

    fun loadTags(booruUid: Long): LiveData<MutableList<TagBlacklist>> {
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                tagBlacklistDao.getTagBlacklistByBooruUidLiveData(booruUid)
            }
            tagOutcome.addSource(data) {
                tagOutcome.postValue(it ?: mutableListOf())
            }
        }
        return tagOutcome
    }
}