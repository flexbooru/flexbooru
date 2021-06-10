package onlymash.flexbooru.data.model.sankaku

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class CommentBody(
    @SerialName("comment")
    val comment: Comment,
    @SerialName("nobump")
    val nobump: Boolean = false
) {
    @Serializable
    data class Comment(
        @SerialName("body")
        val body: String
    )

    companion object {
        fun createBody(body: String): CommentBody {
            return CommentBody(Comment(body))
        }
    }
}