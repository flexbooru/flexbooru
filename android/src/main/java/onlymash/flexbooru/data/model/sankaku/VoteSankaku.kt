package onlymash.flexbooru.data.model.sankaku

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VoteSankaku(
    @SerialName("success")
    val success: Boolean = false,
    @SerialName("post_id")
    var postId: Int = -1
)