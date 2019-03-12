package onlymash.flexbooru.database.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import onlymash.flexbooru.entity.post.PostGel

@Dao
interface PostGelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(posts: List<PostGel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(post: PostGel)

    @Query("SELECT * FROM posts_gelbooru WHERE host = :host AND keyword = :keyword ORDER BY indexInResponse ASC")
    fun getPosts(host: String, keyword: String) : DataSource.Factory<Int, PostGel>

    @Query("SELECT * FROM posts_gelbooru WHERE host = :host AND keyword = :keyword ORDER BY indexInResponse ASC")
    fun getPostsRaw(host: String, keyword: String) : MutableList<PostGel>

    @Query("SELECT * FROM posts_gelbooru WHERE host = :host AND keyword = :keyword ORDER BY indexInResponse ASC LIMIT 1")
    fun getFirstPostRaw(host: String, keyword: String) : PostGel?

    @Query("SELECT * FROM posts_gelbooru WHERE host = :host AND keyword = :keyword ORDER BY indexInResponse ASC")
    fun getPostsLiveData(host: String, keyword: String) : LiveData<MutableList<PostGel>>

    @Query("DELETE FROM posts_gelbooru WHERE host = :host AND keyword = :keyword")
    fun deletePosts(host: String, keyword: String)

    @Query("SELECT MAX(indexInResponse) + 1 FROM posts_gelbooru WHERE host = :host AND keyword = :keyword")
    fun getNextIndex(host: String, keyword: String): Int

    @Delete
    fun deletePost(post: PostGel)

    @Update
    fun updatePost(post: PostGel)

    @Query("DELETE FROM posts_gelbooru WHERE host = :host AND keyword = :keyword AND id = :id")
    fun deletePost(host: String, keyword: String, id: Int)
}