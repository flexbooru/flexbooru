package onlymash.flexbooru.data.model.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TagBase(
    @SerialName("name")
    val name: String,
    @SerialName("category")
    var category: Int = -1
)