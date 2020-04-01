package onlymash.flexbooru.data.model.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BoolResponse(
    @SerialName("success")
    val success: Boolean = false
)