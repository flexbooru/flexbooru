package onlymash.flexbooru.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "pools_moebooru", indices = [(Index(value = ["host", "keyword", "id"], unique = true))])
data class PoolMoe(
    @PrimaryKey(autoGenerate = true)
    var uid: Long = -1L,
    var scheme: String = "",
    var host: String = "",
    var keyword: String = "",
    val id: Int,
    val name: String,
    val created_at: String,
    val updated_at: String,
    val user_id: Int,
    val is_public: Boolean,
    val post_count: Int,
    val description: String
) {
    var indexInResponse: Int = -1
}