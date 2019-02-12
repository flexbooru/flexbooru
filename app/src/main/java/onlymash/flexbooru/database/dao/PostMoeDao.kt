package onlymash.flexbooru.database.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import onlymash.flexbooru.entity.PostMoe

@Dao
interface PostMoeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(posts: List<PostMoe>)

    @Query("SELECT * FROM posts_moebooru WHERE host = :host AND keyword = :keyword ORDER BY indexInResponse ASC")
    fun getPosts(host: String, keyword: String) : DataSource.Factory<Int, PostMoe>

    @Query("SELECT * FROM posts_moebooru WHERE host = :host AND keyword = :keyword ORDER BY indexInResponse ASC")
    fun getPostsRaw(host: String, keyword: String) : MutableList<PostMoe>

    @Query("DELETE FROM posts_moebooru WHERE host = :host AND keyword = :keyword")
    fun deletePosts(host: String, keyword: String)

    @Query("SELECT MAX(indexInResponse) + 1 FROM posts_moebooru WHERE host = :host AND keyword = :keyword")
    fun getNextIndex(host: String, keyword: String): Int
}