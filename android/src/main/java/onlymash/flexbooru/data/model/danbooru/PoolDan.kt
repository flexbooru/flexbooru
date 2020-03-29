package onlymash.flexbooru.data.model.danbooru

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.data.utils.formatDateDan
import onlymash.flexbooru.data.model.common.Pool
@Serializable
data class PoolDan(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("description")
    val description: String,
    @SerialName("is_active")
    val isActive: Boolean,
    @SerialName("is_deleted")
    val isDeleted: Boolean,
    @SerialName("post_count")
    val postCount: Int,
    @SerialName("category")
    val category: String,
    @SerialName("updated_at")
    val updatedAt: String
) {
    fun toPool(scheme: String, host: String): Pool {
        return Pool(
            booruType = BOORU_TYPE_DAN,
            scheme = scheme,
            host = host,
            id = id,
            name = name,
            count = postCount,
            date = updatedAt.formatDateDan().toString(),
            description = description
        )
    }
}