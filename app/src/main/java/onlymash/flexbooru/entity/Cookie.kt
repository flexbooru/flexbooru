package onlymash.flexbooru.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName = "cookies", indices = [(Index(value = ["booru_uid"], unique = true))],
    foreignKeys = [(ForeignKey(
        entity = Booru::class,
        parentColumns = ["uid"],
        childColumns = ["booru_uid"],
        onDelete = ForeignKey.CASCADE))])
data class Cookie(
    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0L,
    var booru_uid: Long,
    var cookie: String?
)