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
import onlymash.flexbooru.database.dao.MuzeiDao
import onlymash.flexbooru.entity.Muzei

class MuzeiViewModel(private val muzeiDao: MuzeiDao) : ScopeViewModel() {

    private val _muzeiOutcome: MediatorLiveData<MutableList<Muzei>> = MediatorLiveData()

    fun loadMuzei(booruUid: Long): LiveData<MutableList<Muzei>> {
        viewModelScope.launch{
            val data = withContext(Dispatchers.IO) {
                muzeiDao.getMuzeiByBooruUidLiveData(booruUid)
            }
            _muzeiOutcome.addSource(data) {
                _muzeiOutcome.postValue(it ?: mutableListOf())
            }
        }
        return _muzeiOutcome
    }

}