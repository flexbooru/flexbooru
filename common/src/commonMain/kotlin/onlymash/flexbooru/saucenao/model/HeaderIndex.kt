package onlymash.flexbooru.saucenao.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HeaderIndex(
    @SerialName("id")
    val id: Int,
    @SerialName("parent_id")
    val parentId: Int,
    @SerialName("results")
    val results: Int,
    @SerialName("status")
    val status: Int
)