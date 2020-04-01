package onlymash.flexbooru.data.model.danbooru1

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.data.model.common.Comment

@Serializable
data class CommentDan1(
    @SerialName("id")
    val id: Int,
    @SerialName("score")
    val score: Int,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("post_id")
    val postId: Int,
    @SerialName("creator")
    val creator: String,
    @SerialName("creator_id")
    val creatorId: Int,
    @SerialName("body")
    val body: String
) {
    fun toComment(): Comment {
        return Comment(
            booruType = BOORU_TYPE_DAN1,
            id = id,
            postId = postId,
            body = body,
            date = createdAt,
            creatorId = creatorId,
            creatorName = creator
        )
    }
}