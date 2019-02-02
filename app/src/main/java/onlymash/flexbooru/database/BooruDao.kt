package onlymash.flexbooru.database

import androidx.room.*
import onlymash.flexbooru.model.Booru

@Dao
interface BooruDao {

    @Query("SELECT * FROM boorus WHERE scheme = :scheme AND host = :host")
    operator fun get(scheme: String, host: String): Booru?

    @Query("SELECT * FROM boorus ORDER BY uid DESC")
    fun getAll(): MutableList<Booru>?

    @Query("SELECT 1 FROM boorus LIMIT 1")
    fun isNotEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(booru: Booru): Long

    @Update
    fun update(booru: Booru): Int

    @Query("DELETE FROM boorus WHERE uid = :uid")
    fun delete(uid: Long): Int

    @Query("DELETE FROM boorus")
    fun deleteAll(): Int
}