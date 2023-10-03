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

package onlymash.flexbooru.data.database

import android.database.sqlite.SQLiteCantOpenDatabaseException
import onlymash.flexbooru.app.App
import onlymash.flexbooru.data.database.dao.BooruDao
import onlymash.flexbooru.data.model.common.Booru
import org.koin.android.ext.android.inject
import java.io.IOException
import java.sql.SQLException

object BooruManager {

    private val booruDao by App.app.inject<BooruDao>()

    @Throws(SQLException::class)
    fun createBooru(booru: Booru): Booru {
        booru.uid = 0
        val uid = booruDao.insert(booru)
        if (uid >= 0) {
            booru.uid = uid
        }
        return booru
    }

    @Throws(SQLException::class)
    fun createBoorus(boorus: List<Booru>) {
        booruDao.insert(boorus)
    }

    @Throws(SQLException::class)
    fun updateBooru(booru: Booru): Boolean = booruDao.update(booru) == 1

    @Throws(IOException::class)
    fun getBooru(scheme: String, host: String): Booru? = try {
        booruDao[scheme, host]
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        null
    }

    @Throws(IOException::class)
    fun getBooruByUid(uid: Long): Booru? = try {
        booruDao.getBooruByUid(uid)
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        null
    }

    @Throws(SQLException::class)
    fun deleteBooru(uid: Long) = booruDao.delete(uid) == 1

    @Throws(SQLException::class)
    fun deleteAll() = booruDao.deleteAll()


    /**
     * return [Boolean]
     * */
    @Throws(IOException::class)
    fun isNotEmpty(): Boolean = try {
        booruDao.isNotEmpty()
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        false
    }

    /**
     * return [Booru] list
     * */
    @Throws(IOException::class)
    fun getAllBoorus(): MutableList<Booru>? = try {
        booruDao.getAll()
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        null
    }
}