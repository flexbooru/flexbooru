package onlymash.flexbooru.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import onlymash.flexbooru.entity.TagDan

@Dao
interface TagDanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(tags: List<TagDan>)
}