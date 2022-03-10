/*
 * Copyright (C) 2020. by onlymash <fiepi.dev@gmail.com>, All rights reserved
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
import onlymash.flexbooru.data.model.common.History

@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(history: History): Long

    @Query("SELECT * FROM `history` WHERE `booru_uid` = :booruUid ORDER BY `uid` DESC")
    fun getHistoryByBooruUid(booruUid: Long): List<History>?

    @Query("SELECT * FROM `history` WHERE `uid` = :uid")
    fun getHistoryByUid(uid: Long): History?

    @Query("SELECT * FROM `history` WHERE `booru_uid` = :booruUid ORDER BY `uid` DESC")
    fun getHistoryByBooruUidLiveData(booruUid: Long): LiveData<List<History>>

    @Delete
    fun delete(history: History): Int

    @Query("DELETE FROM `history` WHERE `uid` = :uid")
    fun deleteByUid(uid: Long): Int

    @Query("DELETE FROM `history` WHERE `booru_uid` = :booruUid")
    fun deleteByBooruUid(booruUid: Long): Int
}