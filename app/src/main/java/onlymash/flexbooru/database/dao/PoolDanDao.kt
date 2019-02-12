package onlymash.flexbooru.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import onlymash.flexbooru.entity.PoolDan

@Dao
interface PoolDanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(pools: List<PoolDan>)
}