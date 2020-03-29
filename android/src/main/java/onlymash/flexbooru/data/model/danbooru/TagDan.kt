package onlymash.flexbooru.data.model.danbooru

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.data.model.common.Tag

@Serializable
data class TagDan(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("post_count")
    val postCount: Int,
    @SerialName("category")
    val category: Int
) {
    fun toTag(): Tag {
        return Tag(
            booruType = BOORU_TYPE_DAN,
            id = id,
            name = name,
            category = category,
            count = postCount
        )
    }
}