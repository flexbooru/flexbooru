package onlymash.flexbooru.ui.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.data.database.dao.BooruDao
import onlymash.flexbooru.data.model.common.Booru

class BooruViewModel(private val booruDao: BooruDao) : ScopeViewModel() {

    private val _boorus: MediatorLiveData<List<Booru>> = MediatorLiveData()

    private val _uid: MutableLiveData<Long> = MutableLiveData(-1L)

    val booru = Transformations.map(_uid) {
        booruDao.getBooruByUid(it)
    }

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
}