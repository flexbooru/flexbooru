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

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.data.database.dao.BooruDao
import onlymash.flexbooru.data.model.common.Booru

class BooruViewModel(private val booruDao: BooruDao) : ScopeViewModel() {

    private val _boorus: MediatorLiveData<List<Booru>> = MediatorLiveData()

    private val _uid: MutableLiveData<Long> = MutableLiveData(-1)
    val booru: MediatorLiveData<Booru> = MediatorLiveData()

    fun loadBoorus(): LiveData<List<Booru>> {
        viewModelScope.launch {
            val boorus = withContext(Dispatchers.IO) {
                booruDao.getAllLiveData()
            }
            _boorus.addSource(boorus) {
                _boorus.postValue(it)
            }
        }
        return _boorus
    }

    fun loadBooru(uid: Long): Boolean {
        if (_uid.value == uid) {
            return false
        }
        _uid.value = uid
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                booruDao.getBooruByUidLiveData(uid)
            }
            booru.addSource(data) {
                booru.postValue(it)
            }
        }
        return true
    }

    fun createBooru(booru: Booru): Long {
        return try {
            booruDao.insert(booru)
        } catch (_: Exception) {
            -1L
        }
    }

    fun createBoorus(boorus: List<Booru>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                booruDao.insert(boorus)
            } catch (_: Exception) {

            }
        }
    }

    fun isNotEmpty() = booruDao.isNotEmpty()

    fun updateBooru(booru: Booru) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                booruDao.update(booru)
            } catch (_: Exception) {}
        }
    }
}