package onlymash.flexbooru.model

data class Popular(
    var scheme: String,
    var host: String,

    // Danbooru: yyyy-mm-dd
    var date: String,
    // Danbooru: day week month
    var scale: String,
    // Moebooru: 1d 1w 1m 1y
    var period: String
)