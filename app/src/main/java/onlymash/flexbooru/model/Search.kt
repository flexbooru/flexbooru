package onlymash.flexbooru.model

data class Search(
    var scheme: String,
    var host: String,
    var limit: Int,
    var keyword: String,
    var username: String,
    var auth_key: String
)