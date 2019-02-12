package onlymash.flexbooru.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tags_danbooru", indices = [(Index(value = ["host", "keyword", "id"], unique = true))])
data class TagDan(
    @PrimaryKey(autoGenerate = true)
    var uid: Long = -1L,
    var host: String = "",
    var keyword: String = "",
    val id: Int,
    val name: String,
    val post_count: Int,
    val related_tags: String,
    val related_tags_updated_at: String,
    val category: Int,
    val created_at: String,
    val updated_at: String,
    val is_locked: Boolean
) {
    var indexInResponse: Int = -1
}