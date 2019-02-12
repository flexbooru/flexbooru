package onlymash.flexbooru.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import onlymash.flexbooru.entity.ArtistDan

@Dao
interface ArtistDanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(artists: List<ArtistDan>)
}