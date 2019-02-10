package onlymash.flexbooru.database

import androidx.room.*
import onlymash.flexbooru.model.User

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE booru_uid = :booruUid")
    fun getUserByBooruUid(booruUid: Long): User?

    @Query("SELECT * FROM users WHERE uid = :uid")
    fun getUserByUserUid(uid: Long): User?

    @Query("SELECT * FROM users ORDER BY uid ASC")
    fun getAll(): MutableList<User>?

    @Query("SELECT 1 FROM boorus LIMIT 1")
    fun isNotEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(user: User): Long

    @Update
    fun update(user: User): Int

    @Query("DELETE FROM users WHERE uid = :uid")
    fun delete(uid: Long): Int

    @Query("DELETE FROM users")
    fun deleteAll(): Int
}