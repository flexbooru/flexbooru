package onlymash.flexbooru.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import onlymash.flexbooru.data.action.ActionPool
import onlymash.flexbooru.data.repository.pool.PoolRepository

class PoolViewModel(private val repository: PoolRepository) : ScopeViewModel() {

    private val _action: MutableLiveData<ActionPool> = MutableLiveData()

    private val _result = Transformations.map(_action) {
        repository.getPools(viewModelScope, it)
    }

    val pools = Transformations.switchMap(_result) { it.pagedList }

    val networkState = Transformations.switchMap(_result) { it.networkState }

    val refreshState = Transformations.switchMap(_result) { it.refreshState }

    fun show(action: ActionPool?): Boolean {
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