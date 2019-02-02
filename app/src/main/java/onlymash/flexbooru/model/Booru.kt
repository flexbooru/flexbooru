package onlymash.flexbooru.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "boorus", indices = [(Index(value = ["scheme", "host"], unique = true))])
data class Booru(
    @PrimaryKey(autoGenerate = true)
    var uid: Long,
    var name: String,
    var scheme: String,
    var host: String,
    var hash_salt: String?,
    // 0: danbooru 1: moebooru
    var type: Int
)