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
import onlymash.flexbooru.entity.Booru
import java.io.IOException
import java.sql.SQLException

object BooruManager {

    interface Listener {
        fun onAdd(booru: Booru)
        fun onDelete(booruUid: Long)
        fun onUpdate(booru: Booru)
    }

    var listeners: MutableList<Listener> = mutableListOf()

    @Throws(SQLException::class)
    fun createBooru(booru: Booru): Booru {
        booru.uid = 0
        val uid = FlexbooruDatabase.booruDao.insert(booru)
        if (uid >= 0) {
            booru.uid = uid
            listeners.forEach {
                it.onAdd(booru)
            }
        }
        return booru
    }

    @Throws(SQLException::class)
    fun updateBooru(booru: Booru): Boolean {
        val result = FlexbooruDatabase.booruDao.update(booru) == 1
        if (result) {
            listeners.forEach {
                it.onUpdate(booru)
            }
        }
        return result
    }

    @Throws(IOException::class)
    fun getBooru(scheme: String, host: String): Booru? = try {
        FlexbooruDatabase.booruDao[scheme, host]
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        ex.printStackTrace()
        null
    }

    @Throws(IOException::class)
    fun getBooruByUid(uid: Long): Booru? = try {
        FlexbooruDatabase.booruDao.getBooruByUid(uid)
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        ex.printStackTrace()
        null
    }

    @Throws(SQLException::class)
    fun deleteBooru(uid: Long) {
        if (FlexbooruDatabase.booruDao.delete(uid) == 1) {
            listeners.forEach {
                it.onDelete(uid)
            }
        }
    }

    @Throws(SQLException::class)
    fun deleteAll() = FlexbooruDatabase.booruDao.deleteAll()


    @Throws(IOException::class)
    fun isNotEmpty(): Boolean = try {
        FlexbooruDatabase.booruDao.isNotEmpty()
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        ex.printStackTrace()
        false
    }

    @Throws(IOException::class)
    fun getAllBoorus(): MutableList<Booru>? = try {
        FlexbooruDatabase.booruDao.getAll()
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        ex.printStackTrace()
        null
    }
}