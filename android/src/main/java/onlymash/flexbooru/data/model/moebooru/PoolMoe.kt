package onlymash.flexbooru.data.model.moebooru

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.common.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.data.model.common.Pool
import onlymash.flexbooru.extension.formatDate
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class PoolMoe(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("user_id")
    val userId: Int,
    @SerialName("is_public")
    val isPublic: Boolean,
    @SerialName("post_count")
    val postCount: Int,
    @SerialName("description")
    val description: String
) {
    private fun date(): CharSequence {
        val date =  when {
            updatedAt.contains("T") -> SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.ENGLISH).parse(updatedAt)
            updatedAt.contains(" ") -> SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(updatedAt)
            else -> {
                throw IllegalStateException("Unknown date format: $updatedAt")
            }
        } ?: return ""
        return date.time.formatDate()
    }

    fun toPool(scheme: String, host: String): Pool {
        return Pool(
            booruType = BOORU_TYPE_MOE,
            scheme = scheme,
            host = host,
            id = id,
            name = name,
            count = postCount,
            date = date(),
            description = description,
            creatorId = userId
        )
    }
}