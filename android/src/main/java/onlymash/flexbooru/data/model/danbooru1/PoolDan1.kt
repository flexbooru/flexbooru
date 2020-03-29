package onlymash.flexbooru.data.model.danbooru1

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.data.model.common.Date
import onlymash.flexbooru.data.model.common.Pool
import onlymash.flexbooru.extension.formatDate

@Serializable
data class PoolDan1(
    @SerialName("user_id")
    val userId: Int,
    @SerialName("is_public")
    val isPublic: Boolean,
    @SerialName("post_count")
    val postCount: Int,
    @SerialName("name")
    val name: String,
    @SerialName("updated_at")
    val updatedAt: Date,
    @SerialName("id")
    val id: Int,
    @SerialName("created_at")
    val createdAt: Date
) {
    fun toPool(scheme: String, host: String): Pool {
        return Pool(
            booruType = BOORU_TYPE_DAN1,
            scheme = scheme,
            host = host,
            id = id,
            name = name,
            count = postCount,
            date = (updatedAt.s * 1000L).formatDate(),
            description = "",
            creatorId = userId
        )
    }
}