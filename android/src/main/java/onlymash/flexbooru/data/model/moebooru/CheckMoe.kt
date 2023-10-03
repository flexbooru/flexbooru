package onlymash.flexbooru.data.model.moebooru
import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName
import onlymash.flexbooru.data.model.common.User


@Serializable
data class CheckMoe(
    @SerialName("exists")
    val exists: Boolean,
    @SerialName("id")
    val id: Int = -1,
    @SerialName("name")
    val name: String,
    @SerialName("no_email")
    val noEmail: Boolean = false,
    @SerialName("pass_hash")
    val passHash: String = "",
    @SerialName("response")
    val response: String,
    @SerialName("success")
    val success: Boolean,
    @SerialName("user_info")
    val userInfo: String = ""
) {
    fun toUser() = User(id = id, name = name, token = passHash)
}