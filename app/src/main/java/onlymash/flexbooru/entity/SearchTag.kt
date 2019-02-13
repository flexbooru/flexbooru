package onlymash.flexbooru.entity

data class SearchTag(
    var scheme: String,
    var host: String,
    var name: String,
    //count name date
    var order: String,
    // Moebooru General: 0, artist: 1, copyright: 3, character: 4, Circle: 5, Faults: 6
    // Danbooru category. 0, 1, 3, 4, 5 (general, artist, copyright, character, meta)
    var type: String,
    var limit: Int,
    var username: String = "",
    var auth_key: String = ""
)