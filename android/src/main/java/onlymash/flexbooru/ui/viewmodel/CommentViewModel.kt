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