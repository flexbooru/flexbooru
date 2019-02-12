package onlymash.flexbooru.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import onlymash.flexbooru.entity.ArtistMoe

@Dao
interface ArtistMoeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(artists: List<ArtistMoe>)
}