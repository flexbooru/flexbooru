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

import onlymash.flexbooru.common.App
import onlymash.flexbooru.database.dao.TagBlacklistDao
import onlymash.flexbooru.entity.common.TagBlacklist
import org.kodein.di.erased.instance
import java.io.IOException
import java.sql.SQLException

object TagBlacklistManager {

    private val tagBlacklistDao: TagBlacklistDao by App.app.instance()
    /**
     * Create a [TagBlacklist]
     * */
    @Throws(SQLException::class)
    fun createTagBlacklist(tagBlacklist: TagBlacklist): TagBlacklist {
        tagBlacklist.uid = 0L
        tagBlacklist.uid = tagBlacklistDao.insert(tagBlacklist)
        return tagBlacklist
    }
    /**
     * Delete a [TagBlacklist]
     * */
    @Throws(SQLException::class)
    fun deleteTagBlacklist(tagBlacklist: TagBlacklist) {
        tagBlacklistDao.delete(tagBlacklist)
    }
    /**
     * return [TagBlacklist] list by booru uid
     * */
    @Throws(IOException::class)
    fun getTagBlacklistByBooruUid(booruUid: Long): MutableList<TagBlacklist> {
        return tagBlacklistDao.getTagBlacklistByBooruUid(booruUid)
    }
}