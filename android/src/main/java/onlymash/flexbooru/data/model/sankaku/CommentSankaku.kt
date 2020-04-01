package onlymash.flexbooru.data.model.sankaku

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.common.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.data.model.common.Comment

@Serializable
data class CommentSankaku(
    @SerialName("body")
    val body: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("creator")
    val creator: String,
    @SerialName("creator_avatar")
    val creatorAvatar: String?,
    @SerialName("creator_id")
    val creatorId: Int,
    @SerialName("id")
    val id: Int,
    @SerialName("post_id")
    val postId: Int,
    @SerialName("score")
    val score: Int
) {
    fun toComment(): Comment {
        return Comment(
            booruType = BOORU_TYPE_SANKAKU,
            id = id,
            postId = postId,
            body = body,
            date = createdAt,
            creatorId = creatorId,
            creatorName = creator,
            creatorAvatar = creatorAvatar
        )
    }
}