package onlymash.flexbooru.data.model.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Artist(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("urls")
    val urls: List<String>? = null
)