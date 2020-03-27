package onlymash.flexbooru.data.model.danbooru

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.data.utils.formatDateDan
import onlymash.flexbooru.data.model.common.Comment
import onlymash.flexbooru.data.model.common.Uploader

@Serializable
data class CommentDan(
    @SerialName("body")
    val body: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("creator")
    val creator: Uploader,
    @SerialName("id")
    val id: Int,
    @SerialName("post_id")
    val postId: Int
) {
    fun toComment(): Comment {
        return Comment(
            id = id,
            postId = postId,
            body = body,
            date = createdAt.formatDateDan().toString(),
            creatorId = creator.id,
            creatorName = creator.name
        )
    }
}