package onlymash.flexbooru.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import onlymash.flexbooru.entity.Suggestion

@Dao
interface SuggestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(suggestion: Suggestion): Long

    @Delete
    fun delete(suggestion: Suggestion)

    @Query("DELETE FROM suggestions WHERE uid = :uid")
    fun delete(uid: Long): Int

    @Query("SELECT * FROM suggestions WHERE booru_uid = :booruUid ORDER BY uid DESC")
    fun getSuggestionsByBooruUid(booruUid: Long): MutableList<Suggestion>?

    @Query("SELECT * FROM suggestions WHERE booru_uid = :booruUid ORDER BY uid DESC")
    fun getSuggestionsByBooruUidLiveData(booruUid: Long): LiveData<MutableList<Suggestion>>?

    @Query("SELECT * FROM suggestions ORDER BY uid ASC")
    fun getAll(): MutableList<Suggestion>?
}