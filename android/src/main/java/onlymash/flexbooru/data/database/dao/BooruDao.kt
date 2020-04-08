/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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
import onlymash.flexbooru.data.model.common.Booru

@Dao
interface BooruDao {

    @Query("SELECT * FROM `boorus` WHERE `scheme` = :scheme AND `host` = :host")
    operator fun get(scheme: String, host: String): Booru?

    @Query("SELECT * FROM `boorus` WHERE `uid` = :uid")
    fun getBooruByUid(uid: Long): Booru?

    @Query("SELECT * FROM `boorus` WHERE `uid` = :uid")
    fun getBooruByUidLiveData(uid: Long): LiveData<Booru?>

    @Query("SELECT * FROM `boorus` ORDER BY `uid` ASC")
    fun getAll(): MutableList<Booru>

    @Query("SELECT * FROM `boorus` ORDER BY `uid` ASC")
    fun getAllLiveData(): LiveData<List<Booru>>

    @Query("SELECT 1 FROM `boorus` LIMIT 1")
    fun isNotEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(booru: Booru): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(boorus: List<Booru>)

    @Update
    fun update(booru: Booru): Int

    @Query("DELETE FROM `boorus` WHERE `uid` = :uid")
    fun delete(uid: Long): Int

    @Query("DELETE FROM `boorus`")
    fun deleteAll(): Int
}