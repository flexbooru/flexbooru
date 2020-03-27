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

package onlymash.flexbooru.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import onlymash.flexbooru.data.model.autocomplete.Suggestion

@Dao
interface SuggestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(suggestion: Suggestion): Long

    @Delete
    fun delete(suggestion: Suggestion)

    @Query("DELETE FROM suggestions WHERE uid = :uid")
    fun delete(uid: Long): Int

    @Query("SELECT * FROM suggestions WHERE booru_uid = :booruUid ORDER BY uid DESC")
    fun getSuggestionsByBooruUid(booruUid: Long): List<Suggestion>

    @Query("SELECT * FROM suggestions WHERE booru_uid = :booruUid ORDER BY uid DESC")
    fun getSuggestionsByBooruUidLiveData(booruUid: Long): LiveData<List<Suggestion>>

    @Query("SELECT * FROM suggestions ORDER BY uid ASC")
    fun getAll(): List<Suggestion>

    @Query("DELETE FROM suggestions")
    fun deleteAll(): Int
}