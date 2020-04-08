/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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
import androidx.paging.DataSource
import androidx.room.*
import onlymash.flexbooru.data.model.common.Post

@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(posts: List<Post>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(post: Post)

    @Query("SELECT * FROM `posts` WHERE `booru_uid` = :booruUid AND `query` = :query ORDER BY `index` ASC")
    fun getPosts(booruUid: Long, query: String) : DataSource.Factory<Int, Post>

    @Query("SELECT * FROM `posts` WHERE `booru_uid` = :booruUid AND `query` = :query ORDER BY `index` ASC LIMIT :limit")
    fun getPostsRaw(booruUid: Long, query: String, limit: Int) : List<Post>

    @Query("SELECT * FROM `posts` WHERE `booru_uid` = :booruUid AND `query` = :query ORDER BY `index` ASC LIMIT 1")
    fun getFirstPostRaw(booruUid: Long, query: String) : Post?

    @Query("SELECT * FROM `posts` WHERE `booru_uid` = :booruUid AND `id` = :postId ORDER BY `index` ASC LIMIT 1")
    fun getFirstPostLiveData(booruUid: Long, postId: Int) : LiveData<Post?>

    @Query("SELECT * FROM `posts` WHERE `booru_uid` = :booruUid AND `query` = :query ORDER BY `index` ASC")
    fun getPostsLiveData(booruUid: Long, query: String) : LiveData<List<Post>>

    @Query("DELETE FROM `posts` WHERE `booru_uid` = :booruUid AND `query` = :query")
    fun deletePosts(booruUid: Long, query: String)

    @Query("SELECT MAX(`index`) + 1 FROM `posts` WHERE `booru_uid` = :booruUid AND `query` = :query")
    fun getNextIndex(booruUid: Long, query: String): Int

    @Delete
    fun deletePost(post: Post)

    @Update
    fun updatePost(post: Post)

    @Query("DELETE FROM `posts` WHERE `booru_uid` = :booruUid AND `query` = :query AND id = :id")
    fun deletePost(booruUid: Long, query: String, id: Int)

    @Query("DELETE FROM `posts`")
    fun deleteAll()

    @Query("UPDATE `posts` SET `is_favored` = :isFavored WHERE `booru_uid` = :booruUid AND `id` = :postId")
    fun updateFav(booruUid: Long, postId: Int, isFavored: Boolean)
}