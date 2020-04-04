package onlymash.flexbooru.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import onlymash.flexbooru.data.action.ActionArtist
import onlymash.flexbooru.data.repository.artist.ArtistRepository

class ArtistViewModel(private val repository: ArtistRepository) : ScopeViewModel() {

    private val _action: MutableLiveData<ActionArtist?> = MutableLiveData()

    private val _result = Transformations.map(_action) { action ->
        if (action != null) {
            repository.getArtists(viewModelScope, action)
        } else {
            null
        }
    }

    val artists = Transformations.switchMap(_result) { it?.pagedList }

    val networkState = Transformations.switchMap(_result) { it?.networkState }

    val refreshState = Transformations.switchMap(_result) { it?.refreshState }

    fun show(action: ActionArtist?): Boolean {
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