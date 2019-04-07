package onlymash.flexbooru.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import onlymash.flexbooru.entity.Cookie

@Dao
interface CookieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cookie: Cookie): Long

    @Query("SELECT * FROM cookies WHERE booru_uid = :booruUid")
    fun getCookieByBooruUid(booruUid: Long): Cookie?
}