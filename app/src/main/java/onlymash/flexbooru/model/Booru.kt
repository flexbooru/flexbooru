package onlymash.flexbooru.model

data class Booru(
    var uid: Int?,
    var name: String,
    var scheme: String,
    var host: String,
    var hash_salt: String?,
    // 0: danbooru 1: moebooru
    var type: Int
)