package onlymash.flexbooru.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import onlymash.flexbooru.data.action.ActionTag
import onlymash.flexbooru.data.repository.tag.TagRepository

class TagViewModel(private val repository: TagRepository) : ScopeViewModel() {

    private val _action: MutableLiveData<ActionTag?> = MutableLiveData()

    private val _result = Transformations.map(_action) { action ->
        if (action != null) {
            repository.getTags(viewModelScope, action)
        } else {
            null
        }
    }

    val tags = Transformations.switchMap(_result) { it?.pagedList }

    val networkState = Transformations.switchMap(_result) { it?.networkState }

    val refreshState = Transformations.switchMap(_result) { it?.refreshState }

    fun show(action: ActionTag?): Boolean {
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