package onlymash.flexbooru.data.model.common

import androidx.room.*

@Entity(tableName = "cookies", indices = [(Index(value = ["host"], unique = true))])
data class Cookie(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "uid")
    var uid: Long = 0L,
    @ColumnInfo(name = "host")
    var host: String,
    @ColumnInfo(name = "cookie")
    var cookie: String
)
