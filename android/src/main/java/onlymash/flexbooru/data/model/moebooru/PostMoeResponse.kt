package onlymash.flexbooru.data.model.moebooru

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostMoeResponse(
    @SerialName("posts")
    val posts: List<PostMoe>,
    @SerialName("tags")
    val tags: Map<String, String>,
    @SerialName("votes")
    val votes: Map<String, Int> = mapOf()
)

@Serializable
data class PostMoeLoliResponse(
    @SerialName("posts")
    val posts: List<PostMoe>,
    @SerialName("tags")
    val tags: Map<String, String>
)