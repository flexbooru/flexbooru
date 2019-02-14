package onlymash.flexbooru.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "artists_danbooru", indices = [(Index(value = ["host", "keyword", "id"], unique = true))])
data class ArtistDan(
    @PrimaryKey(autoGenerate = true)
    var uid: Long = -1,
    var scheme: String = "",
    var host: String = "",
    var keyword: String? = "",
    val id: Int,
    val name: String,
    val created_at: String,
    val updated_at: String,
    val creator_id: Int,
    val is_active: Boolean,
    val group_name: String,
    val is_banned: Boolean,
    val other_names: MutableList<String>?,
    val urls: MutableList<ArtistUrlDan>?
) {
    var indexInResponse: Int = -1
}

data class ArtistUrlDan(
    val id: Int,
    val artist_id: Int,
    val url: String,
    val normalized_url: String,
    val created_at: String,
    val updated_at: String,
    val is_active: Boolean
)