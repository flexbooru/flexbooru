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
import onlymash.flexbooru.entity.TagFilter
import java.io.IOException
import java.sql.SQLException

object TagFilterManager {

    interface Listener {
        fun onAdd(tag: TagFilter)
        fun onDelete(tag: TagFilter)
        fun onUpdate(tag: TagFilter)
    }

    val listeners: MutableList<Listener> = mutableListOf()

    @Throws(SQLException::class)
    fun createTagFilter(tag: TagFilter): TagFilter {
        tag.uid = 0
        tag.uid = FlexbooruDatabase.tagFilterDao.insert(tag)
        if (tag.uid >= 0) {
            listeners.forEach {
                it.onAdd(tag)
            }
        }
        return tag
    }

    @Throws(SQLException::class)
    fun updateTagFilter(tag: TagFilter): Boolean {
        val result = FlexbooruDatabase.tagFilterDao.update(tag) == 1
        if (result) {
            listeners.forEach {
                it.onUpdate(tag)
            }
        }
        return result
    }

    @Throws(IOException::class)
    fun getTagFilterByBooruUid(booruUid: Long): MutableList<TagFilter>? = try {
        FlexbooruDatabase.tagFilterDao.getTagByBooruUid(booruUid)
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        ex.printStackTrace()
        null
    }

    @Throws(IOException::class)
    fun getTagFilterByTagFilterUid(uid: Long): TagFilter? = try {
        FlexbooruDatabase.tagFilterDao.getTagByTagUid(uid)
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        ex.printStackTrace()
        null
    }

    @Throws(SQLException::class)
    fun deleteTagFilter(tag: TagFilter) {
        if (FlexbooruDatabase.tagFilterDao.delete(tag.uid) == 1) {
            listeners.forEach {
                it.onDelete(tag)
            }
        }
    }

    @Throws(SQLException::class)
    fun deleteAll() = FlexbooruDatabase.tagFilterDao.deleteAll()

    @Throws(IOException::class)
    fun getAllTagFilters(): MutableList<TagFilter>? = try {
        FlexbooruDatabase.tagFilterDao.getAll()
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        ex.printStackTrace()
        null
    }
}