package onlymash.flexbooru.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "pools_danbooru", indices = [(Index(value = ["host", "keyword", "id"], unique = true))])
data class PoolDan(
    @PrimaryKey(autoGenerate = true)
    var uid: Long = -1L,
    var host: String = "",
    var keyword: String = "",
    val id: Int,
    val name: String,
    val created_at: String,
    val updated_at: String,
    val creator_id: Int,
    val description: String,
    val is_active: Boolean,
    val is_deleted: Boolean,
    val category: String,
    val creator_name: String,
    val post_count: Int
) {
    var indexInResponse: Int = -1
}