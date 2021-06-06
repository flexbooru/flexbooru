package onlymash.flexbooru.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import onlymash.flexbooru.data.model.common.Next

@Dao
interface NextDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(next: Next): Long

    @Query("SELECT * FROM `next` WHERE `booru_uid` = :booruUid AND `query` = :query")
    fun getNext(booruUid: Long, query: String): Next?

    @Query("DELETE FROM `next` WHERE `booru_uid` = :booruUid AND `query` = :query")
    fun delete(booruUid: Long, query: String): Int
}