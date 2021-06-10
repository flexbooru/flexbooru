package onlymash.flexbooru.data.model.sankaku
import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName


@Serializable
data class LoginBody(
    @SerialName("login")
    val login: String,
    @SerialName("password")
    val password: String
)