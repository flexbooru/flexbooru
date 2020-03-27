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

package onlymash.flexbooru.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import onlymash.flexbooru.data.model.common.TagFilter

@Dao
interface TagFilterDao {
    @Query("SELECT * FROM tags_filter WHERE booru_uid = :booruUid")
    fun getTagByBooruUid(booruUid: Long): List<TagFilter>?

    @Query("SELECT * FROM tags_filter WHERE uid = :uid")
    fun getTagByTagUid(uid: Long): TagFilter?

    @Query("SELECT * FROM tags_filter ORDER BY uid ASC")
    fun getAll(): List<TagFilter>?

    @Query("SELECT 1 FROM boorus LIMIT 1")
    fun isNotEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(tag: TagFilter): Long

    @Update
    fun update(tag: TagFilter): Int

    @Query("DELETE FROM tags_filter WHERE uid = :uid")
    fun delete(uid: Long): Int

    @Delete
    fun delete(tags: List<TagFilter>)

    @Query("DELETE FROM tags_filter")
    fun deleteAll(): Int

    @Query("SELECT * FROM tags_filter WHERE booru_uid = :booruUid")
    fun getTagByBooruUidLiveData(booruUid: Long): LiveData<List<TagFilter>>

    @Query("SELECT * FROM tags_filter ORDER BY name ASC")
    fun getAllLiveData(): LiveData<List<TagFilter>>
}