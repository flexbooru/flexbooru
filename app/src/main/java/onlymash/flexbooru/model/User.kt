package onlymash.flexbooru.model

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
    var uid: Long,
    var booru_uid: Long?,
    var name: String,
    var id: Int,
    var password_hash: String?,
    var api_key: String?
)