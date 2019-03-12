package onlymash.flexbooru.ui.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import onlymash.flexbooru.database.SuggestionManager
import onlymash.flexbooru.entity.Suggestion

class SuggestionViewModel : ViewModel() {

    val suggestions: MediatorLiveData<MutableList<Suggestion>> = MediatorLiveData()

    fun loadSuggestions(booruUid: Long) {
        suggestions.addSource(
            SuggestionManager.getSuggestionsByBooruUidLiveData(booruUid) ?: MutableLiveData()) {
            suggestions.postValue(it ?: mutableListOf())
        }
    }
}