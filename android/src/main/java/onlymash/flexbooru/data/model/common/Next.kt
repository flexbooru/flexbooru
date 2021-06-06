package onlymash.flexbooru.data.model.common

import androidx.room.*

@Entity(tableName = "next", indices = [(Index(value = ["booru_uid", "query"], unique = true))],
    foreignKeys = [(ForeignKey(
        entity = Booru::class,
        parentColumns = ["uid"],
        childColumns = ["booru_uid"],
        onDelete = ForeignKey.CASCADE))])
data class Next(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "uid")
    var uid: Long = 0L,
    @ColumnInfo(name = "booru_uid")
    val booruUid: Long,
    @ColumnInfo(name = "query")
    val query: String,
    @ColumnInfo(name = "next")
    var next: String?
)