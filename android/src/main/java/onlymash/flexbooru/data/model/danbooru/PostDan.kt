package onlymash.flexbooru.data.model.danbooru

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.data.utils.formatDateDan
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.data.model.common.TagBase
import onlymash.flexbooru.data.model.common.Uploader
import onlymash.flexbooru.data.utils.toSafeUrl

@Serializable
data class PostDan(
    @SerialName("id")
    val id: Int,
    @SerialName("pixiv_id")
    val pixivId: Int?,
    @SerialName("parent_id")
    val parentId: Int?,
    @SerialName("rating")
    val rating: String,
    @SerialName("score")
    val score: Int,
    @SerialName("source")
    val source: String,
    @SerialName("fav_count")
    val favCount: Int,
    @SerialName("image_height")
    val imageHeight: Int,
    @SerialName("image_width")
    val imageWidth: Int,
    @SerialName("file_ext")
    val fileExt: String?,
    @SerialName("file_size")
    val fileSize: Int,
    @SerialName("preview_file_url")
    val previewFileUrl: String?,
    @SerialName("large_file_url")
    val largeFileUrl: String?,
    @SerialName("file_url")
    val fileUrl: String?,
    @SerialName("tag_string")
    val tagString: String,
    @SerialName("tag_string_artist")
    val tagStringArtist: String,
    @SerialName("tag_string_character")
    val tagStringCharacter: String,
    @SerialName("tag_string_copyright")
    val tagStringCopyright: String,
    @SerialName("tag_string_general")
    val tagStringGeneral: String,
    @SerialName("tag_string_meta")
    val tagStringMeta: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String?,
    @SerialName("is_favorited")
    val isFavorited: Boolean = false,
    @SerialName("uploader")
    val uploader: UploaderDan
) {
    private fun previewUrl(scheme: String, host: String) =
        previewFileUrl?.toSafeUrl(scheme, host) ?: ""

    private fun sampleUrl(scheme: String, host: String) =
        largeFileUrl?.toSafeUrl(scheme, host) ?: previewUrl(scheme, host)

    private fun mediumUrl(scheme: String, host: String) = sampleUrl(scheme, host)

    private fun originUrl(scheme: String, host: String) =
        fileUrl?.toSafeUrl(scheme, host) ?: sampleUrl(scheme, host)

    fun toPost(booruUid: Long, query: String, scheme: String, host: String, index: Int): Post {
        return Post(
            booruUid = booruUid,
            query = query,
            index = index,
            id = id,
            width = imageWidth,
            height = imageHeight,
            size = fileSize,
            score = score,
            rating = rating,
            isFavored = isFavorited,
            date = createdAt.formatDateDan().toString(),
            tags = getTags(),
            preview = previewUrl(scheme, host),
            sample = sampleUrl(scheme, host),
            medium = mediumUrl(scheme, host),
            origin = originUrl(scheme, host),
            pixivId = pixivId,
            source = source,
            uploader = Uploader(id = uploader.id, name = uploader.name)
        )
    }

    private fun getTags(): List<TagBase>{
        val tagsGeneral = tagStringGeneral.toTagsList(0)
        val tagsCharacter = tagStringCharacter.toTagsList(1)
        val tagsCopyright = tagStringCopyright.toTagsList(2)
        val tagsMeta = tagStringMeta.toTagsList(3)
        val tagsArtist = tagStringArtist.toTagsList(4)
        return tagsArtist + tagsMeta + tagsCopyright + tagsCharacter + tagsGeneral
    }

    private fun String.toTagsList(type: Int) =
        trim().split(" ").map { TagBase(it, type) }
}

@Serializable
data class UploaderDan(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("level")
    val level: Int,
    @SerialName("level_string")
    val levelString: String,
    @SerialName("post_update_count")
    val postUpdateCount: Int,
    @SerialName("post_upload_count")
    val postUploadCount: Int
)