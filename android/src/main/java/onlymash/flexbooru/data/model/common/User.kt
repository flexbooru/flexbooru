package onlymash.flexbooru.data.model.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("id")
    var id: Int = -1,
    @SerialName("name")
    var name: String = "",
    @SerialName("token")
    var token: String = "",
    @SerialName("avatar")
    var avatar: String? = null
)