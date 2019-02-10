package onlymash.flexbooru.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "posts_moebooru", indices = [(Index(value = ["host", "keyword", "id"], unique = true))])
data class PostMoe(
    @Optional
    @PrimaryKey(autoGenerate = true)
    val uid: Long = -1,
    @Optional
    var host: String = "",
    @Optional
    var keyword: String = "",
    val id: Int,
    val tags: String?,
    val created_at: Int,
    val creator_id: Int,
    val author: String,
    val change: Int,
    val source: String?,
    val score: Int,
    val md5: String,
    val file_size: Int = 0,
    val file_url: String?,
    val is_shown_in_index: Boolean,
    val preview_url: String,
    val preview_width: Int,
    val preview_height: Int,
    val actual_preview_width: Int,
    val actual_preview_height: Int,
    val sample_url: String,
    val sample_width: Int,
    val sample_height: Int,
    val sample_file_size: Int,
    val jpeg_url: String?,
    val jpeg_width: Int = 0,
    val jpeg_height: Int = 0,
    val jpeg_file_size: Int = 0,
    val rating: String,
    val has_children: Boolean,
    val parent_id: Int?,
    val status: String,
    val width: Int,
    val height: Int,
    val is_held: Boolean
) {
    var indexInResponse: Int = -1
}