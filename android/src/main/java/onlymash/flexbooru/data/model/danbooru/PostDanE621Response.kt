package onlymash.flexbooru.data.model.danbooru

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostDanE621Response(
    @SerialName("posts")
    val posts: List<PostDanE621>
)