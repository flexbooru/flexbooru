package onlymash.flexbooru.data.model.sankaku

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.app.Values
import onlymash.flexbooru.data.model.common.Comment
import onlymash.flexbooru.data.model.common.Date
import onlymash.flexbooru.data.utils.toSafeUrl

@Serializable
data class CommentSankakuItem(
    @SerialName("author")
    val author: AuthorSankaku,
    @SerialName("body")
    val body: String,
    @SerialName("created_at")
    val createdAt: Date,
    @SerialName("id")
    val id: Int,
    @SerialName("post_id")
    val postId: Int,
) {
    fun toComment(scheme: String, host: String): Comment {
        return Comment(
            booruType = Values.BOORU_TYPE_SANKAKU,
            id = id,
            postId = postId,
            body = body,
            time = createdAt.s * 1000L,
            creatorId = author.id,
            creatorName = author.name,
            creatorAvatar = author.avatar?.toSafeUrl(scheme, host)
        )
    }
}