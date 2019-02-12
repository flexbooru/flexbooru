package onlymash.flexbooru.entity

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class SearchPopular(
    var scheme: String,
    var host: String,

    // Danbooru: yyyy-mm-dd
    @Optional
    var date: String = "",

    // Danbooru: day week month
    @Optional
    var scale: String = "day",

    // Moebooru: 1d 1w 1m 1y
    @Optional
    var period: String = "1d",

    @Optional
    var username: String = "",
    @Optional
    var auth_key: String = "",

    @Optional
    var safe_mode: Boolean = true
)