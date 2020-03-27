package onlymash.flexbooru.data.model.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Date(
    @SerialName("n")
    val n: Long,
    @SerialName("s")
    val s: Long
)