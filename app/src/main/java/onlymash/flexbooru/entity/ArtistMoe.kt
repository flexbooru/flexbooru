package onlymash.flexbooru.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "artists_moebooru", indices = [(Index(value = ["host", "keyword", "id"], unique = true))])
data class ArtistMoe(
    @PrimaryKey(autoGenerate = true)
    var uid: Long = -1L,
    var scheme: String = "",
    var host: String = "",
    var keyword: String? = "",
    val id: Int,
    val name: String,
    val alias_id: Int?,
    val group_id: Int?,
    val urls: MutableList<String>?
) {
    var indexInResponse: Int = -1
}