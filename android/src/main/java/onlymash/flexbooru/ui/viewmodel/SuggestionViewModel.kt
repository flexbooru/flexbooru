package onlymash.flexbooru.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import onlymash.flexbooru.data.action.ActionTag
import onlymash.flexbooru.data.repository.suggestion.SuggestionRepository

class SuggestionViewModel(private val repo: SuggestionRepository) : ScopeViewModel() {

    val suggestions: MutableLiveData<List<String>> = MutableLiveData()

    fun fetchSuggestions(actionTag: ActionTag) {
        viewModelScope.launch {
            val tags = repo.fetchSuggestions(actionTag)?.map { it.name }
            suggestions.postValue(tags?: listOf())
        }
    }
}