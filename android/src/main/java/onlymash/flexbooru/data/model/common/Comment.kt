package onlymash.flexbooru.data.model.common

data class Comment(
    val id: Int,
    val postId: Int,
    val body: String,
    val date: String,
    val creatorId: Int,
    val creatorName: String,
    val creatorAvatar: String? = null
)