package onlymash.flexbooru.data.model.danbooru

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.data.model.common.Tag

@Serializable
data class TagDan(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("post_count")
    val postCount: Int,
    @SerialName("related_tags")
    val relatedTags: String,
    @SerialName("related_tags_updated_at")
    val relatedTagsUpdatedAt: String,
    @SerialName("category")
    val category: Int,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("is_locked")
    val isLocked: Boolean
) {
    fun toTag(): Tag {
        return Tag(
            id = id,
            name = name,
            category = category,
            count = postCount
        )
    }
}