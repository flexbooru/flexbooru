package onlymash.flexbooru.entity.artist

abstract class BaseArtist {
    var scheme: String = ""
    var host: String = ""
    var keyword: String? = ""
    abstract fun getArtistId(): Int
    abstract fun getArtistName(): String
    abstract fun getArtistUrls(): MutableList<String>
}