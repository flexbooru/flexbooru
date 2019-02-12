package onlymash.flexbooru.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import onlymash.flexbooru.entity.TagMoe

@Dao
interface TagMoeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(tags: List<TagMoe>)
}