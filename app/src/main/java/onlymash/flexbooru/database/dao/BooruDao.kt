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

import androidx.room.*
import onlymash.flexbooru.entity.Booru

@Dao
interface BooruDao {

    @Query("SELECT * FROM boorus WHERE scheme = :scheme AND host = :host")
    operator fun get(scheme: String, host: String): Booru?

    @Query("SELECT * FROM boorus WHERE uid = :uid")
    fun getBooruByUid(uid: Long): Booru?

    @Query("SELECT * FROM boorus ORDER BY uid ASC")
    fun getAll(): MutableList<Booru>?

    @Query("SELECT 1 FROM boorus LIMIT 1")
    fun isNotEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(booru: Booru): Long

    @Update
    fun update(booru: Booru): Int

    @Query("DELETE FROM boorus WHERE uid = :uid")
    fun delete(uid: Long): Int

    @Query("DELETE FROM boorus")
    fun deleteAll(): Int
}