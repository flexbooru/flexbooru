package onlymash.flexbooru.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [(Index(value = ["booru_uid"], unique = true))],
    foreignKeys = [(ForeignKey(
            entity = Booru::class,
            parentColumns = ["uid"],
            childColumns = ["booru_uid"],
            onDelete = ForeignKey.CASCADE))])
data class User(
    @PrimaryKey(autoGenerate = true)
    var uid: Long = -1L,
    var booru_uid: Long = -1L,
    var name: String,
    var id: Int,
    var password_hash: String? = null,
    var api_key: String? = null
)