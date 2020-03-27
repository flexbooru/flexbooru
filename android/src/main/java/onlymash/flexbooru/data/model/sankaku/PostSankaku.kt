package onlymash.flexbooru.data.model.sankaku

import androidx.room.ColumnInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.data.model.common.Date
import onlymash.flexbooru.data.model.common.TagBase
import onlymash.flexbooru.data.model.common.Uploader
import onlymash.flexbooru.data.utils.toSafeUrl
import onlymash.flexbooru.extension.formatDate


@Serializable
data class PostSankaku(
    @SerialName("author")
    val author: AuthorSankaku,
    @SerialName("created_at")
    val createdAt: Date,
    @SerialName("fav_count")
    val favCount: Int,
    @SerialName("file_size")
    val fileSize: Int,
    @SerialName("file_url")
    val fileUrl: String,
    @SerialName("has_children")
    val hasChildren: Boolean,
    @SerialName("height")
    val height: Int,
    @SerialName("id")
    val id: Int,
    @SerialName("is_favorited")
    val isFavorited: Boolean,
    @SerialName("parent_id")
    val parentId: Int?,
    @SerialName("preview_height")
    val previewHeight: Int,
    @SerialName("preview_url")
    val previewUrl: String,
    @SerialName("preview_width")
    val previewWidth: Int,
    @SerialName("rating")
    val rating: String?,
    @SerialName("sample_height")
    val sampleHeight: Int,
    @SerialName("sample_url")
    val sampleUrl: String,
    @ColumnInfo(name = "sample_width")
    @SerialName("sample_width")
    val sampleWidth: Int,
    @ColumnInfo(name = "source")
    @SerialName("source")
    val source: String?,
    @SerialName("tags")
    val tags: List<TagSankaku>,
    @SerialName("total_score")
    val totalScore: Int,
    @SerialName("vote_count")
    val voteCount: Int,
    @SerialName("width")
    val width: Int
) {

    private fun previewUrl(scheme: String, host: String) =
        previewUrl.toSafeUrl(scheme, host)

    private fun sampleUrl(scheme: String, host: String) =
        sampleUrl.toSafeUrl(scheme, host)

    private fun mediumUrl(scheme: String, host: String) = sampleUrl(scheme, host)

    private fun originUrl(scheme: String, host: String) =
        fileUrl.toSafeUrl(scheme, host)

    private fun String.getTags(): List<TagBase> =
        trim().split(" ").map { TagBase(it, -1) }

    private fun TagSankaku.toTagBase(): TagBase = TagBase(name, type)

    private fun List<TagSankaku>.toTags() = map { it.toTagBase() }

    fun toPost(booruUid: Long, query: String, scheme: String, host: String, index: Int): Post {
        return Post(
            booruUid = booruUid,
            query = query,
            index = index,
            id = id,
            width = width,
            height = height,
            size = fileSize,
            score = totalScore,
            rating = rating ?: "e",
            date = (createdAt.s * 1000L).formatDate().toString(),
            tags = tags.toTags(),
            preview = previewUrl(scheme, host),
            sample = sampleUrl(scheme, host),
            medium = mediumUrl(scheme, host),
            origin = originUrl(scheme, host),
            source = source,
            isFavored = isFavorited,
            uploader = Uploader(id = author.id, name = author.name, avatar = author.avatar)
        )
    }

}