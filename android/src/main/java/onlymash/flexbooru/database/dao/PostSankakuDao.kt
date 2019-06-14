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

package onlymash.flexbooru.database.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import onlymash.flexbooru.entity.post.PostSankaku

@Dao
interface PostSankakuDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(posts: List<PostSankaku>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(post: PostSankaku)

    @Query("SELECT * FROM posts_sankaku WHERE host = :host AND keyword = :keyword ORDER BY indexInResponse ASC")
    fun getPosts(host: String, keyword: String) : DataSource.Factory<Int, PostSankaku>

    @Query("SELECT * FROM posts_sankaku WHERE host = :host AND keyword = :keyword ORDER BY indexInResponse ASC")
    fun getPostsRaw(host: String, keyword: String) : MutableList<PostSankaku>

    @Query("SELECT * FROM posts_sankaku WHERE host = :host AND keyword = :keyword ORDER BY indexInResponse ASC LIMIT 1")
    fun getFirstPostRaw(host: String, keyword: String) : PostSankaku?

    @Query("SELECT * FROM posts_sankaku WHERE host = :host AND keyword = :keyword ORDER BY indexInResponse ASC")
    fun getPostsLiveData(host: String, keyword: String) : LiveData<MutableList<PostSankaku>>

    @Query("DELETE FROM posts_sankaku WHERE host = :host AND keyword = :keyword")
    fun deletePosts(host: String, keyword: String)

    @Query("SELECT MAX(indexInResponse) + 1 FROM posts_sankaku WHERE host = :host AND keyword = :keyword")
    fun getNextIndex(host: String, keyword: String): Int

    @Delete
    fun deletePost(post: PostSankaku)

    @Update
    fun updatePost(post: PostSankaku)

    @Query("DELETE FROM posts_sankaku WHERE host = :host AND keyword = :keyword AND id = :id")
    fun deletePost(host: String, keyword: String, id: Int)
}