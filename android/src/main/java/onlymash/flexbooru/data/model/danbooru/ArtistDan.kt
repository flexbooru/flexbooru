package onlymash.flexbooru.data.model.danbooru
import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName
import onlymash.flexbooru.data.model.common.Artist

@Serializable
data class ArtistDan(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("urls")
    val urls: List<ArtistDanUrl>
) {
    fun toArtist(): Artist {
        return Artist(
            id = id,
            name = name,
            urls = urls.map { it.url }
        )
    }
}

@Serializable
data class ArtistDanUrl(
    @SerialName("artist_id")
    val artistId: Int,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("id")
    val id: Int,
    @SerialName("is_active")
    val isActive: Boolean,
    @SerialName("normalized_url")
    val normalizedUrl: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("url")
    val url: String
)