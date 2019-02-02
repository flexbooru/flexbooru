package onlymash.flexbooru.model

data class Search(
    var scheme: String,
    var host: String,
    var limit: Int,
    var tags: String
)