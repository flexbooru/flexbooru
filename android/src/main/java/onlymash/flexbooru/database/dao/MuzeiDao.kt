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

package onlymash.flexbooru.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import onlymash.flexbooru.entity.Muzei

@Dao
interface MuzeiDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(muzei: Muzei): Long

    @Query("SELECT * FROM muzei WHERE booru_uid = :booruUid ORDER BY uid ASC")
    fun getMuzeiByBooruUid(booruUid: Long): MutableList<Muzei>?

    @Query("SELECT * FROM muzei WHERE booru_uid = :booruUid ORDER BY uid ASC")
    fun getMuzeiByBooruUidLiveData(booruUid: Long): LiveData<MutableList<Muzei>>

    @Delete
    fun delete(muzei: Muzei): Int
}