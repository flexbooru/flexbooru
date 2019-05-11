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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import onlymash.flexbooru.database.SuggestionManager
import onlymash.flexbooru.entity.Suggestion
import onlymash.flexbooru.extension.ioMain

class SuggestionViewModel : ViewModel() {

    private val suggestions: MediatorLiveData<MutableList<Suggestion>> = MediatorLiveData()

    fun loadSuggestions(booruUid: Long): LiveData<MutableList<Suggestion>> {
        ioMain({
            SuggestionManager.getSuggestionsByBooruUidLiveData(booruUid) ?: MutableLiveData()
        }){ data ->
            suggestions.addSource(data) {
                suggestions.postValue(it ?: mutableListOf())
            }
        }
        return suggestions
    }
}