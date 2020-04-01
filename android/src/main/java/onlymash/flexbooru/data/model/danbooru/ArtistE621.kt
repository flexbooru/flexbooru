package onlymash.flexbooru.data.model.danbooru
import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName
import onlymash.flexbooru.data.model.common.Artist

@Serializable
data class ArtistE621(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String
) {
    fun toArtist(): Artist = Artist(id, name, null)
}