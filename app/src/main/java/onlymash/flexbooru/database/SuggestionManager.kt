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

package onlymash.flexbooru.database

import android.database.SQLException
import android.database.sqlite.SQLiteCantOpenDatabaseException
import androidx.lifecycle.LiveData
import com.crashlytics.android.Crashlytics
import onlymash.flexbooru.entity.Suggestion
import java.io.IOException

object SuggestionManager {

    @Throws(SQLException::class)
    fun createSuggestion(suggestion: Suggestion): Suggestion {
        suggestion.uid = 0L
        suggestion.uid = FlexbooruDatabase.suggestionDao.insert(suggestion)
        return suggestion
    }

    @Throws(SQLException::class)
    fun deleteSuggestion(uid: Long): Boolean = FlexbooruDatabase.suggestionDao.delete(uid) == 1

    @Throws(IOException::class)
    fun getSuggestionsByBooruUid(booruUid: Long): MutableList<Suggestion>? = try {
        FlexbooruDatabase.suggestionDao.getSuggestionsByBooruUid(booruUid)
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        Crashlytics.logException(ex)
        null
    }

    @Throws(IOException::class)
    fun getSuggestionsByBooruUidLiveData(booruUid: Long): LiveData<MutableList<Suggestion>>? = try {
        FlexbooruDatabase.suggestionDao.getSuggestionsByBooruUidLiveData(booruUid)
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        Crashlytics.logException(ex)
        null
    }
}