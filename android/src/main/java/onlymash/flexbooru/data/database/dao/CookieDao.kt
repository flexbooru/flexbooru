package onlymash.flexbooru.data.database.dao

import androidx.room.*
import onlymash.flexbooru.data.model.common.Cookie

@Dao
interface CookieDao {
    @Query("SELECT * FROM `cookies` WHERE :host LIKE '%.' || `host`")
    fun getCookie(host: String): Cookie?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cookie: Cookie): Long

    @Update
    fun update(cookie: Cookie): Int

    @Query("DELETE FROM `cookies` WHERE `uid` = :uid")
    fun delete(uid: Long): Int

    @Query("DELETE FROM `cookies` WHERE `host` = :host")
    fun deleteByHost(host: String): Int
}