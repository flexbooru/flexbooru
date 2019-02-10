package onlymash.flexbooru.model

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class Search(
    var scheme: String,
    var host: String,
    var limit: Int,
    var keyword: String,
    @Optional
    var username: String = "",
    @Optional
    var auth_key: String = ""
)