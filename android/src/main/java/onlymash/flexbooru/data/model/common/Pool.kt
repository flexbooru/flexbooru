package onlymash.flexbooru.data.model.common

data class Pool(
    val booruType: Int = -1,
    val scheme: String = "https",
    val host: String,
    val id: Int,
    val name: String,
    val count: Int,
    val date: CharSequence,
    val description: String,
    val creatorId: Int = -1,
    val creatorName: String? = null,
    val creatorAvatar: String? = null
)