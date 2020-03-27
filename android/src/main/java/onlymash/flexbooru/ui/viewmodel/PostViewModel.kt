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