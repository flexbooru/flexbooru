package onlymash.flexbooru.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "posts_danbooru", indices = [(Index(value = ["host", "keyword", "id"], unique = true))])
data class PostDan(
    @Optional
    @PrimaryKey(autoGenerate = true)
    val uid: Long = -1,
    @Optional
    var host: String = "",
    @Optional
    var keyword: String = "",
    val id: Int,
    val created_at: String,
    val uploader_id: Int,
    val score: Int,
    val source: String,
    val md5: String?,
    val last_comment_bumped_at: String?,
    val rating: String,
    val image_width: Int,
    val image_height: Int,
    val tag_string: String,
    val is_note_locked: Boolean,
    val fav_count: Int,
    val file_ext: String?,
    val last_noted_at: String?,
    val is_rating_locked: Boolean,
    val parent_id: Int?,
    val has_children: Boolean,
    val approver_id: Int?,
    val tag_count_general: Int,
    val tag_count_artist: Int,
    val tag_count_character: Int,
    val tag_count_copyright: Int,
    val file_size: Int,
    val is_status_locked: Boolean,
    val pool_string: String?,
    val up_score: Int,
    val down_score: Int,
    val is_pending: Boolean,
    val is_flagged: Boolean,
    val is_deleted: Boolean,
    val tag_count: Int,
    val updated_at: String,
    val is_banned: Boolean,
    val pixiv_id: Int,
    val last_commented_at: String?,
    val has_active_children: Boolean,
    val bit_flags: Int,
    val tag_count_meta: Int,
    val uploader_name: String,
    val has_large: Boolean,
    val has_visible_children: Boolean,
    val children_ids: String?,
    val is_favorited: Boolean,
    val tag_string_general: String,
    val tag_string_character: String,
    val tag_string_copyright: String,
    val tag_string_artist: String,
    val tag_string_meta: String,
    val file_url: String?,
    val large_file_url: String?,
    val preview_file_url: String?
) {
    var indexInResponse: Int = -1
}