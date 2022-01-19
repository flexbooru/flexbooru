package onlymash.flexbooru.data.model.gelbooru

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.app.Values
import onlymash.flexbooru.data.model.common.Tag

@Serializable
@SerialName("tag")
data class TagGelLegacy(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("count")
    val count: Int,
    @SerialName("type")
    val type: Int
) {
    fun toTag(): Tag {
        return Tag(
            booruType = Values.BOORU_TYPE_GEL,
            id = id,
            name = name,
            category = type,
            count = count
        )
    }
}