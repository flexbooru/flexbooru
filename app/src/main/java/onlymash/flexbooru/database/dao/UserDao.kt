/*
 * Copyright (C) 2019 by onlymash <im@fiepi.me>, All rights reserved
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
import onlymash.flexbooru.entity.User

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE booru_uid = :booruUid")
    fun getUserByBooruUid(booruUid: Long): User?

    @Query("SELECT * FROM users WHERE uid = :uid")
    fun getUserByUserUid(uid: Long): User?

    @Query("SELECT * FROM users ORDER BY uid ASC")
    fun getAll(): MutableList<User>?

    @Query("SELECT 1 FROM boorus LIMIT 1")
    fun isNotEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(user: User): Long

    @Update
    fun update(user: User): Int

    @Query("DELETE FROM users WHERE uid = :uid")
    fun delete(uid: Long): Int

    @Query("DELETE FROM users")
    fun deleteAll(): Int
}