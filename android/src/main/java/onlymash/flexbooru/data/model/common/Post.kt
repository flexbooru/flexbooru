package onlymash.flexbooru.data.model.common

import androidx.room.*


@Entity(tableName = "posts", indices = [(Index(value = ["booru_uid", "query", "id"], unique = true))],
    foreignKeys = [(ForeignKey(
        entity = Booru::class,
        parentColumns = ["uid"],
        childColumns = ["booru_uid"],
        onDelete = ForeignKey.CASCADE))])
data class Post(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "uid")
    var uid: Long = 0L,
    @ColumnInfo(name = "booru_uid")
    var booruUid: Long = -1L,
    @ColumnInfo(name = "index")
    var index: Int = -1,
    @ColumnInfo(name = "query")
    var query: String = "",

    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "width")
    val width: Int,
    @ColumnInfo(name = "height")
    val height: Int,
    @ColumnInfo(name = "size")
    val size: Int,
    @ColumnInfo(name = "score")
    val score: Int,
    @ColumnInfo(name = "rating")
    val rating: String,
    @ColumnInfo(name = "is_favored")
    var isFavored: Boolean = false,
    @ColumnInfo(name = "date")
    val date: String,
    @ColumnInfo(name = "tags")
    val tags: List<TagBase>,
    @ColumnInfo(name = "preview")
    val preview: String,
    @ColumnInfo(name = "sample")
    val sample: String,
    @ColumnInfo(name = "medium")
    val medium: String,
    @ColumnInfo(name = "origin")
    val origin: String,
    @ColumnInfo(name = "pixiv_id")
    val pixivId: Int? = null,
    @ColumnInfo(name = "source")
    val source: String?,
    @ColumnInfo(name = "uploader")
    val uploader: User
)