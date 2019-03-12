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

package onlymash.flexbooru.database

import android.database.sqlite.SQLiteCantOpenDatabaseException
import onlymash.flexbooru.entity.User
import java.io.IOException
import java.sql.SQLException

object UserManager {

    interface Listener {
        fun onAdd(user: User)
        fun onDelete(user: User)
        fun onUpdate(user: User)
    }

    val listeners: MutableList<Listener> = mutableListOf()

    @Throws(SQLException::class)
    fun createUser(user: User): User {
        user.uid = 0
        user.uid = FlexbooruDatabase.userDao.insert(user)
        if (user.uid >= 0) {
            listeners.forEach {
                it.onAdd(user)
            }
        }
        return user
    }

    @Throws(SQLException::class)
    fun updateUser(user: User): Boolean {
        val result = FlexbooruDatabase.userDao.update(user) == 1
        if (result) {
            listeners.forEach {
                it.onUpdate(user)
            }
        }
        return result
    }

    @Throws(IOException::class)
    fun getUserByBooruUid(booruUid: Long): User? = try {
        FlexbooruDatabase.userDao.getUserByBooruUid(booruUid)
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        ex.printStackTrace()
        null
    }

    @Throws(IOException::class)
    fun getUserByUserUid(uid: Long): User? = try {
        FlexbooruDatabase.userDao.getUserByUserUid(uid)
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        ex.printStackTrace()
        null
    }

    @Throws(SQLException::class)
    fun deleteUser(user: User) {
        if (FlexbooruDatabase.userDao.delete(user.uid) == 1) {
            listeners.forEach {
                it.onDelete(user)
            }
        }
    }

    @Throws(SQLException::class)
    fun deleteAll() = FlexbooruDatabase.userDao.deleteAll()

    @Throws(IOException::class)
    fun getAllUsers(): MutableList<User>? = try {
        FlexbooruDatabase.userDao.getAll()
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        ex.printStackTrace()
        null
    }
}