package onlymash.flexbooru.entity

data class SearchArtist(
    var scheme: String,
    var host: String,
    //search keyword
    var name: String,
    // danbooru: name, updated_at, post_count (Defaults to ID).
    // moebooru: name, date
    var order: String,
    var limit: Int,
    var username: String = "",
    var auth_key: String = ""
)