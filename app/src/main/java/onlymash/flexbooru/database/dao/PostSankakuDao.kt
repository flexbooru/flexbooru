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