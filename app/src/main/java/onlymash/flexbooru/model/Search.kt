package onlymash.flexbooru.model

data class Search(
    val scheme: String,
    val host: String,
    val limit: Int,
    val tags: String
)