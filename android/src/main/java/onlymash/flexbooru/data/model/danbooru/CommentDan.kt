package onlymash.flexbooru.data.model.danbooru

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.data.utils.formatDateDan
import onlymash.flexbooru.data.model.common.Comment
import onlymash.flexbooru.data.model.common.User

@Serializable
data class CommentDan(
    @SerialName("body")
    val body: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("creator")
    val creator: User? = null,
    @SerialName("id")
    val id: Int,
    @SerialName("post_id")
    val postId: Int,
    @SerialName("creator_id")
    val creatorId: Int? = -1,
    @SerialName("creator_name")
    val creatorName: String? = null
) {
    fun toComment(): Comment {
        return Comment(
            booruType = BOORU_TYPE_DAN,
            id = id,
            postId = postId,
            body = body,
            date = createdAt.formatDateDan().toString(),
            creatorId = creator?.id ?: creatorId ?: -1,
            creatorName = creator?.name ?: creatorName ?: ""
        )
    }
}