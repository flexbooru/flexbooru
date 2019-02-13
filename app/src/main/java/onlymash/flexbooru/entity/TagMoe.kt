package onlymash.flexbooru.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tags_moebooru", indices = [(Index(value = ["host", "id"], unique = true))])
data class TagMoe(
    @PrimaryKey(autoGenerate = true)
    var uid: Long = -1L,
    var scheme: String = "",
    var host: String = "",
    val id: Int,
    val name: String,
    val count: Int,
    val type: Int,
    val ambiguous: Boolean
) {
    var indexInResponse: Int = -1
}