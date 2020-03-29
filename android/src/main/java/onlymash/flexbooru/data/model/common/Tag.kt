package onlymash.flexbooru.data.model.common

data class Tag(
    val booruType: Int = -1,
    val id: Int,
    val name: String,
    val category: Int,
    val count: Int
)