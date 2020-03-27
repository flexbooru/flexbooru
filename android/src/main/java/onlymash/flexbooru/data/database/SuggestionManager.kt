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

package onlymash.flexbooru.data.database

import android.database.SQLException
import android.database.sqlite.SQLiteCantOpenDatabaseException
import androidx.lifecycle.LiveData
import onlymash.flexbooru.common.App
import onlymash.flexbooru.data.database.dao.SuggestionDao
import onlymash.flexbooru.data.model.autocomplete.Suggestion
import org.kodein.di.erased.instance
import java.io.IOException

object SuggestionManager {

    private val suggestionDao: SuggestionDao by App.app.instance()

    @Throws(SQLException::class)
    fun createSuggestion(suggestion: Suggestion): Suggestion {
        suggestion.uid = 0L
        suggestion.uid = suggestionDao.insert(suggestion)
        return suggestion
    }

    @Throws(SQLException::class)
    fun deleteSuggestion(uid: Long): Boolean = suggestionDao.delete(uid) == 1

    @Throws(SQLException::class)
    fun deleteAll() = suggestionDao.deleteAll()

    @Throws(IOException::class)
    fun getSuggestionsByBooruUid(booruUid: Long): List<Suggestion>? = try {
        suggestionDao.getSuggestionsByBooruUid(booruUid)
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        null
    }

    @Throws(IOException::class)
    fun getSuggestionsByBooruUidLiveData(booruUid: Long): LiveData<List<Suggestion>> = try {
        suggestionDao.getSuggestionsByBooruUidLiveData(booruUid)
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        throw IOException(ex)
    }
}