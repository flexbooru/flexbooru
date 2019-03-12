package onlymash.flexbooru.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "suggestions", indices = [(Index(value = ["booru_uid", "keyword"], unique = true))],
    foreignKeys = [(ForeignKey(
        entity = Booru::class,
        parentColumns = ["uid"],
        childColumns = ["booru_uid"],
        onDelete = ForeignKey.CASCADE))])
data class Suggestion(
    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0L,
    var booru_uid: Long = 0L,
    val keyword: String
)