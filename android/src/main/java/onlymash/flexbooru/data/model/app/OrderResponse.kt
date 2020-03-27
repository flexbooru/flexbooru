package onlymash.flexbooru.data.model.app

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("activated")
    val activated: Boolean
)
