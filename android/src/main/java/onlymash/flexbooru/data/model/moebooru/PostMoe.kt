package onlymash.flexbooru.data.model.moebooru

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.data.model.common.TagBase
import onlymash.flexbooru.data.model.common.User
import onlymash.flexbooru.data.utils.toSafeUrl
import onlymash.flexbooru.extension.formatDate

@Serializable
data class PostMoe(
    @SerialName("id")
    val id: Int,
    @SerialName("tags")
    val tags: String?,
    @SerialName("created_at")
    val createdAt: Int,
    @SerialName("creator_id")
    val creatorId: Int,
    @SerialName("author")
    val author: String,
    @SerialName("source")
    val source: String?,
    @SerialName("score")
    val score: Int,
    @SerialName("file_size")
    val fileSize: Int?,
    @SerialName("file_url")
    val fileUrl: String?,
    @SerialName("preview_url")
    val previewUrl: String,
    @SerialName("preview_width")
    val previewWidth: Int,
    @SerialName("preview_height")
    val previewHeight: Int,
    @SerialName("sample_url")
    val sampleUrl: String?,
    @SerialName("sample_width")
    val sampleWidth: Int,
    @SerialName("sample_height")
    val sampleHeight: Int,
    @SerialName("sample_file_size")
    val sampleFileSize: Int?,
    @SerialName("jpeg_url")
    val jpegUrl: String?,
    @SerialName("jpeg_width")
    val jpegWidth: Int?,
    @SerialName("jpeg_height")
    val jpegHeight: Int?,
    @SerialName("jpeg_file_size")
    val jpegFileSize: Int?,
    @SerialName("rating")
    val rating: String,
    @SerialName("has_children")
    val hasChildren: Boolean,
    @SerialName("parent_id")
    val parentId: Int?,
    @SerialName("status")
    val status: String,
    @SerialName("width")
    val width: Int,
    @SerialName("height")
    val height: Int
) {
    private fun previewUrl(scheme: String, host: String) =
        previewUrl.toSafeUrl(scheme, host)

    private fun sampleUrl(scheme: String, host: String) =
        sampleUrl?.toSafeUrl(scheme, host) ?: previewUrl(scheme, host)

    private fun mediumUrl(scheme: String, host: String) =
        jpegUrl?.toSafeUrl(scheme, host) ?: sampleUrl(scheme, host)

    private fun originUrl(scheme: String, host: String) =
        fileUrl?.toSafeUrl(scheme, host) ?: sampleUrl(scheme, host)

    private fun String.getTags(): List<TagBase> =
        trim().split(" ").map { TagBase(it, -1) }

    fun toPost(booruUid: Long, query: String, scheme: String, host: String, index: Int): Post {
        return Post(
            booruUid = booruUid,
            query = query,
            index = index,
            id = id,
            width = width,
            height = height,
            size = fileSize ?: 0,
            score = score,
            rating = rating,
            date = (createdAt * 1000L).formatDate().toString(),
            tags = tags?.getTags() ?: listOf(),
            preview = previewUrl(scheme, host),
            sample = sampleUrl(scheme, host),
            medium = mediumUrl(scheme, host),
            origin = originUrl(scheme, host),
            source = source,
            uploader = User(id = creatorId, name = author)
        )
    }
}