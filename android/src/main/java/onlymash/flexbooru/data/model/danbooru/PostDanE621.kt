package onlymash.flexbooru.data.model.danbooru
import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.data.model.common.TagBase
import onlymash.flexbooru.data.model.common.User
import onlymash.flexbooru.data.utils.formatDateDan

@Serializable
data class PostDanE621(
    @SerialName("created_at")
    val createdAt: String?,
    @SerialName("description")
    val description: String,
    @SerialName("fav_count")
    val favCount: Int,
    @SerialName("file")
    val `file`: File,
    @SerialName("id")
    val id: Int,
    @SerialName("is_favorited")
    val isFavorited: Boolean,
    @SerialName("preview")
    val preview: Preview,
    @SerialName("rating")
    val rating: String,
    @SerialName("sample")
    val sample: Sample,
    @SerialName("score")
    val score: Score,
    @SerialName("sources")
    val sources: List<String>,
    @SerialName("tags")
    val tags: Tags,
    @SerialName("updated_at")
    val updatedAt: String?,
    @SerialName("uploader_id")
    val uploaderId: Int
) {
    @Serializable
    data class File(
        @SerialName("ext")
        val ext: String,
        @SerialName("height")
        val height: Int,
        @SerialName("size")
        val size: Int,
        @SerialName("url")
        val url: String?,
        @SerialName("width")
        val width: Int
    )

    @Serializable
    data class Preview(
        @SerialName("height")
        val height: Int,
        @SerialName("url")
        val url: String?,
        @SerialName("width")
        val width: Int
    )

    @Serializable
    data class Sample(
        @SerialName("has")
        val has: Boolean,
        @SerialName("height")
        val height: Int,
        @SerialName("url")
        val url: String?,
        @SerialName("width")
        val width: Int
    )

    @Serializable
    data class Score(
        @SerialName("down")
        val down: Int,
        @SerialName("total")
        val total: Int,
        @SerialName("up")
        val up: Int
    )

    @Serializable
    data class Tags(
        @SerialName("artist")
        val artist: List<String>,
        @SerialName("character")
        val character: List<String>,
        @SerialName("copyright")
        val copyright: List<String>,
        @SerialName("general")
        val general: List<String>,
        @SerialName("invalid")
        val invalid: List<String>,
        @SerialName("lore")
        val lore: List<String>,
        @SerialName("meta")
        val meta: List<String>,
        @SerialName("species")
        val species: List<String>
    )

    fun toPost(booruUid: Long, query: String, index: Int) = Post(
        booruUid = booruUid,
        query = query,
        index = index,
        id = id,
        width = file.width,
        height = file.height,
        size = file.size,
        score = score.total,
        rating = rating,
        isFavored = isFavorited,
        date = createdAt?.formatDateDan().toString(),
        tags = getTags(),
        preview = preview.url ?: "",
        sample = sample.url ?: "",
        medium = sample.url ?: "",
        origin = file.url ?: "",
        pixivId = -1,
        source = sources.toString(),
        uploader = User(id = uploaderId, name = "null")
    )

    private fun getTags(): List<TagBase> {
        val tagsGeneral = tags.general.map { TagBase(name = it, category = 0) }
        val tagsCharacter = tags.character.map { TagBase(name = it, category = 1) }
        val tagsCopyright = tags.copyright.map { TagBase(name = it, category = 2) }
        val tagsMeta = tags.meta.map { TagBase(name = it, category = 3) }
        val tagsArtist = tags.artist.map { TagBase(name = it, category = 4) }
        val tagsLore = tags.lore.map { TagBase(name = it, category = 5) }
        val tagsSpecies = tags.species.map { TagBase(name = it, category = 6) }
        val tagsInvalid = tags.invalid.map { TagBase(name = it, category = 7) }
        return tagsArtist + tagsMeta + tagsCopyright + tagsCharacter +
                tagsGeneral + tagsLore + tagsSpecies + tagsInvalid
    }
}
