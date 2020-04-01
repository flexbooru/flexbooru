package onlymash.flexbooru.data.model.moebooru

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.common.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.data.model.common.Comment
import onlymash.flexbooru.extension.formatDate
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class CommentMoe(
    @SerialName("id")
    val id: Int,
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
    private fun date(): CharSequence {
        val date =  when {
            createdAt.contains("T") -> SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.ENGLISH).parse(createdAt)
            createdAt.contains(" ") -> SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(createdAt)
            else -> throw IllegalStateException("Unknown date format: $createdAt")
        } ?: return ""
        return date.time.formatDate()
    }

    fun toComment(): Comment {
        return Comment(
            booruType = BOORU_TYPE_MOE,
            id = id,
            postId = postId,
            body = body,
            date = date().toString(),
            creatorId = creatorId,
            creatorName = creator
        )
    }
}