package onlymash.flexbooru.data.model.common

data class Pool(
    val id: Int,
    val name: String,
    val count: Int,
    val date: CharSequence,
    val description: String,
    val creatorId: Int = -1,
    val creatorName: String? = null
)