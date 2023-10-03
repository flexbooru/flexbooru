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
import onlymash.flexbooru.data.database.dao.TagFilterDao
import onlymash.flexbooru.data.model.common.TagFilter
import org.koin.android.ext.android.inject
import java.io.IOException
import java.sql.SQLException

object TagFilterManager {

    private val tagFilterDao by App.app.inject<TagFilterDao>()

    @Throws(SQLException::class)
    fun createTagFilter(tag: TagFilter): TagFilter {
        tag.uid = 0
        tag.uid = tagFilterDao.insert(tag)
        return tag
    }

    @Throws(SQLException::class)
    fun updateTagFilter(tag: TagFilter): Boolean = tagFilterDao.update(tag) == 1

    @Throws(IOException::class)
    fun getTagFilterByBooruUid(booruUid: Long): List<TagFilter>? = try {
        tagFilterDao.getTagByBooruUid(booruUid)
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        ex.printStackTrace()
        null
    }

    @Throws(IOException::class)
    fun getTagFilterByTagFilterUid(uid: Long): TagFilter? = try {
        tagFilterDao.getTagByTagUid(uid)
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        ex.printStackTrace()
        null
    }

    @Throws(SQLException::class)
    fun deleteTagFilter(tag: TagFilter) {
        tagFilterDao.delete(tag.uid)
    }

    @Throws(SQLException::class)
    fun deleteTagsFilter(tags: List<TagFilter>) {
        tagFilterDao.delete(tags)
    }

    @Throws(SQLException::class)
    fun deleteAll() = tagFilterDao.deleteAll()

    @Throws(IOException::class)
    fun getAllTagFilters(): List<TagFilter>? = try {
        tagFilterDao.getAll()
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        ex.printStackTrace()
        null
    }
}