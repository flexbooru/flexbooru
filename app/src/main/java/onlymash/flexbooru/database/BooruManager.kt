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

import android.database.sqlite.SQLiteCantOpenDatabaseException
import com.crashlytics.android.Crashlytics
import onlymash.flexbooru.common.App
import onlymash.flexbooru.database.dao.BooruDao
import onlymash.flexbooru.entity.Booru
import org.kodein.di.generic.instance
import java.io.IOException
import java.sql.SQLException

object BooruManager {

    interface Listener {
        fun onAdd(booru: Booru)
        fun onDelete(booruUid: Long)
        fun onUpdate(booru: Booru)
        fun onChanged(boorus: MutableList<Booru>)
    }

    // booru change callback
    val listeners: MutableList<Listener> = mutableListOf()

    private val booruDao: BooruDao by App.app.instance()

    @Throws(SQLException::class)
    fun createBooru(booru: Booru): Booru {
        booru.uid = 0
        val uid = booruDao.insert(booru)
        if (uid >= 0) {
            booru.uid = uid
            listeners.forEach {
                it.onAdd(booru)
            }
        }
        return booru
    }

    @Throws(SQLException::class)
    fun createBoorus(boorus: MutableList<Booru>) {
        booruDao.insert(boorus)
        val bs = getAllBoorus() ?: mutableListOf()
        listeners.forEach {
            it.onChanged(bs)
        }
    }

    @Throws(SQLException::class)
    fun updateBooru(booru: Booru): Boolean {
        val result = booruDao.update(booru) == 1
        if (result) {
            listeners.forEach {
                it.onUpdate(booru)
            }
        }
        return result
    }

    @Throws(IOException::class)
    fun getBooru(scheme: String, host: String): Booru? = try {
        booruDao[scheme, host]
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        Crashlytics.logException(ex)
        null
    }

    @Throws(IOException::class)
    fun getBooruByUid(uid: Long): Booru? = try {
        booruDao.getBooruByUid(uid)
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        Crashlytics.logException(ex)
        null
    }

    @Throws(SQLException::class)
    fun deleteBooru(uid: Long) {
        if (booruDao.delete(uid) == 1) {
            listeners.forEach {
                it.onDelete(uid)
            }
        }
    }

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
        Crashlytics.logException(ex)
        false
    }

    /**
     * return [Booru] list
     * */
    @Throws(IOException::class)
    fun getAllBoorus(): MutableList<Booru>? = try {
        booruDao.getAll()
    } catch (ex: SQLiteCantOpenDatabaseException) {
        Crashlytics.logException(ex)
        throw IOException(ex)
    } catch (ex: SQLException) {
        Crashlytics.logException(ex)
        null
    }
}